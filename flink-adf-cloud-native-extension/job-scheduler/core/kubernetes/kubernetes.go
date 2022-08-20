package kubernetes

import (
	"bytes"
	"context"
	"errors"
	"io"
	"log"
	"os"

	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/azure-sdk-for-go/sdk/resourcemanager/containerservice/armcontainerservice"
	"github.com/Azure/go-autorest/autorest/adal"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"
	"k8s.io/client-go/tools/clientcmd"

	"job-scheduler/core/logger"
)

const (
	subscriptionId = "SUBSCRIPTION_ID"
	resourceGroup  = "RESOURCE_GROUP"
	cluster        = "CLUSTER"
	clientId       = "CLIENT_ID"

	JobNamespace   = "job"
	FlinkNamespace = "flink"
)

func GetPodLog(ctx context.Context, clientset *kubernetes.Clientset, namespace string, podName string) (log string, err error) {
	podLogOpts := v1.PodLogOptions{}
	req := clientset.CoreV1().Pods(namespace).GetLogs(podName, &podLogOpts)
	podLogs, err := req.Stream(ctx)
	if err != nil {
		logger.GetLog().Errorf("req Stream error: %v", err)
		return "", err
	}
	defer podLogs.Close()

	buf := new(bytes.Buffer)
	_, err = io.Copy(buf, podLogs)
	if err != nil {
		logger.GetLog().Errorf("io Copy error: %v", err)
		return "", err
	}
	result := buf.String()
	return result, nil
}

func GetPodsByJob(ctx context.Context, clientset *kubernetes.Clientset, namespace string, jobName string) (podList *v1.PodList, err error) {
	pods, err := clientset.CoreV1().Pods(namespace).List(ctx, metav1.ListOptions{LabelSelector: "job-name=" + jobName})
	if err != nil {
		logger.GetLog().Errorf("get pod by job error: %v", err)
		return nil, err
	}
	return pods, nil
}

func GetPodsByDeployment(ctx context.Context, clientset *kubernetes.Clientset, namespace string, deploymentName string) (podList *v1.PodList, err error) {
	pods, err := clientset.CoreV1().Pods(namespace).List(ctx, metav1.ListOptions{LabelSelector: "app=" + deploymentName})
	if err != nil {
		logger.GetLog().Errorf("get pod by deployment error: %v", err)
		return nil, err
	}
	return pods, nil
}

func GetKubernetesClientSetWithEnv(ctx context.Context) (client *kubernetes.Clientset, err error) {
	clientConfig, err := GetKubernetesClientConfigWithEnv(ctx)
	if err != nil {
		logger.GetLog().Errorf("GetKubernetesClientConfigWithEnv error: %v", err)
		return nil, err
	}

	clientset, err := GetKubernetesClientSet(clientConfig)
	if err != nil {
		logger.GetLog().Errorf("GetKubernetesClientSet error: %v", err)
		return nil, err
	}
	return clientset, nil
}

func GetKubernetesClientConfigWithEnv(ctx context.Context) (config clientcmd.ClientConfig, err error) {
	subscriptionID := os.Getenv(subscriptionId)
	resourceGroup := os.Getenv(resourceGroup)
	cluster := os.Getenv(cluster)
	clientID := os.Getenv(clientId)
	return GetKubernetesClientConfig(ctx, subscriptionID, resourceGroup, cluster, clientID)
}

func GetKubeConfigBytes(ctx context.Context, subscriptionID string, resourceGroup string, cluster string) (kubeConfigBytes []byte, err error) {
	cred, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		logger.GetLog().Errorf("azidentity NewDefaultAzureCredential error: %v", err)
		return nil, err
	}

	managedClustersClient, err := armcontainerservice.NewManagedClustersClient(subscriptionID, cred, nil)
	if err != nil {
		logger.GetLog().Errorf("armcontainerservice NewManagedClustersClient error: %v", err)
		return nil, err
	}

	accessProfile, err := managedClustersClient.ListClusterUserCredentials(ctx, resourceGroup, cluster, nil)
	if err != nil {
		logger.GetLog().Errorf("managedClustersClient ListClusterUserCredentials error: %v", err)
		return nil, err
	}

	kubeConfigs := accessProfile.CredentialResults.Kubeconfigs
	if len(kubeConfigs) != 1 {
		logger.GetLog().Errorf("accessProfile CredentialResults Kubeconfigs lens larger than 1.")
		return nil, errors.New("accessProfile CredentialResults Kubeconfigs lens larger than 1.")
	}

	return kubeConfigs[0].Value, nil
}

func GetKubernetesClientConfig(ctx context.Context, subscriptionID string, resourceGroup string, cluster string, clientID string) (config clientcmd.ClientConfig, err error) {
	kubeConfigBytes, err := GetKubeConfigBytes(ctx, subscriptionID, resourceGroup, cluster)
	if err != nil {
		logger.GetLog().Errorf("GetKubeConfigBytes error: %v", err)
		return nil, err
	}

	clientConfig, err := clientcmd.NewClientConfigFromBytes(kubeConfigBytes)
	if err != nil {
		logger.GetLog().Errorf("clientcmd NewClientConfigFromBytes error: %v", err)
		return nil, err
	}

	rawConfig, err := clientConfig.RawConfig()
	if err != nil {
		logger.GetLog().Errorf("clientConfig RawConfig error: %v", err)
		return nil, err
	}

	callback := func(t adal.Token) error {
		return nil
	}
	overrides := &clientcmd.ConfigOverrides{}
	for _, authInfo := range rawConfig.AuthInfos {
		serverID := authInfo.AuthProvider.Config["apiserver-id"]
		spt, err := adal.NewServicePrincipalTokenFromManagedIdentity(
			serverID,
			&adal.ManagedIdentityOptions{
				ClientID: clientID,
			},
			callback)
		if err != nil {
			logger.GetLog().Errorf("adal NewServicePrincipalTokenFromManagedIdentity error: %v", err)
			return nil, err
		}
		spt.Refresh()
		token := spt.Token()
		log.Print(token.AccessToken)
		log.Print(token.ExpiresOn)
		authInfo.AuthProvider.Config["access-token"] = token.AccessToken
		authInfo.AuthProvider.Config["expires-on"] = string(token.ExpiresOn)
		overrides.AuthInfo.Token = token.AccessToken
	}

	overrides.Context.Namespace = JobNamespace

	clientConfig = clientcmd.NewDefaultClientConfig(rawConfig, overrides)
	return clientConfig, nil
}

func GetKubernetesClientSet(clientConfig clientcmd.ClientConfig) (client *kubernetes.Clientset, err error) {
	restConfig, err := clientConfig.ClientConfig()
	if err != nil {
		log.Fatal("clientcmd NewDefaultClientConfig error: %v", err)
		return nil, err
	}

	log.Print(restConfig)

	clientset, err := kubernetes.NewForConfig(restConfig)
	if err != nil {
		log.Fatal("kubernetes.NewForConfig error: %v", err)
		return nil, err
	}

	return clientset, nil
}
