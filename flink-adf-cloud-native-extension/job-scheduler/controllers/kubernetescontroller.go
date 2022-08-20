package controllers

import (
	"context"
	"io/ioutil"
	"net/http"

	"github.com/gin-gonic/gin"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"

	"job-scheduler/core/flink"
	"job-scheduler/core/helm"
	"job-scheduler/core/kubernetes"
	"job-scheduler/core/logger"
	"job-scheduler/core/result"
)

type FlinkJobRequestBody struct {
	ArtifactId string `json:"artifact_id"`
	JobId      string `json:"job_id"`
}

const (
	CHART_PATH          = "deployment/flink-deployer"
	RELEASE_NAME        = "flink-deployer"
)

func CreateFlinkDeploymentJob(ginContext *gin.Context) {
	ctx := context.Background()

	clientConfig, err := kubernetes.GetKubernetesClientConfigWithEnv(ctx)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetKubernetesClientConfigWithEnv error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	actionConfig, err := helm.GetActionConfig(clientConfig, kubernetes.JobNamespace)
	if err != nil {
		logger.GetLog().Errorf("helm GetActionConfig error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	releases, err := helm.HelmList(actionConfig)
	if err != nil {
		logger.GetLog().Errorf("helm HelmList error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	hasRelease := false
	for _, release := range releases {
		if release.Name == RELEASE_NAME {
			hasRelease = true
			break
		}
	}

	if hasRelease {
		err = helm.HelmUninstall(actionConfig, RELEASE_NAME)
		if err != nil {
			logger.GetLog().Errorf("helm HelmUninstall error: %v", err)
			panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
		}
	}

	chart, err := helm.GetHelmChart(CHART_PATH)
	if err != nil {
		logger.GetLog().Errorf("helm GetHelmChart error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	chart, err = helm.OverwriteEnvADLSGen2AccountKey(chart)
	if err != nil {
		logger.GetLog().Errorf("helm OverwriteEnvADLSGen2AccountKey error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	err = helm.HelmInstall(actionConfig, chart, RELEASE_NAME)
	if err != nil {
		logger.GetLog().Errorf("helm HelmInstall error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	ginContext.JSON(http.StatusOK, result.Success("success", nil))
}

func GetFlinkDeploymentStatus(ginContext *gin.Context) {
	ctx := context.Background()

	clientset, err := kubernetes.GetKubernetesClientSetWithEnv(ctx)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetKubernetesClientSet error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	pods, err := kubernetes.GetPodsByJob(ctx, clientset, kubernetes.JobNamespace, RELEASE_NAME)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetPodsByJob error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	data := make(map[string]string)
	if len(pods.Items) == 0 {
		data["status"] = "UNKNOWN"
	} else {
		data["status"] = string(pods.Items[0].Status.Phase)
	}
	ginContext.JSON(http.StatusOK, result.Success("success", data))
}

func GetFlinkDeploymentLog(ginContext *gin.Context) {
	ctx := context.Background()

	clientset, err := kubernetes.GetKubernetesClientSetWithEnv(ctx)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetKubernetesClientSet error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	pods, err := kubernetes.GetPodsByJob(ctx, clientset, kubernetes.JobNamespace, RELEASE_NAME)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetPodsByJob error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	podName := pods.Items[0].GetName()

	logStr, err := kubernetes.GetPodLog(ctx, clientset, kubernetes.JobNamespace, podName)

	ginContext.JSON(http.StatusOK, result.Success("success", logStr))
}

func GetFlinkJobStatus(ginContext *gin.Context) {
	var flinkJobMeta FlinkJobRequestBody
	err := ginContext.BindJSON(&flinkJobMeta)
	if err != nil {
		logger.GetLog().Errorf("ginContext BindJSON error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	req, err := http.NewRequest(http.MethodGet, flink.GetFlinkHistoryServerURL()+"jobs/"+flinkJobMeta.JobId, nil)
	if err != nil {
		logger.GetLog().Errorf("http NewRequest error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		logger.GetLog().Errorf("http Call error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	resBody, err := ioutil.ReadAll(res.Body)
	if err != nil {
		logger.GetLog().Errorf("ioutil ReadAll error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	ginContext.Data(http.StatusOK, gin.MIMEJSON, resBody)
}

func GetFlinkJobID(ginContext *gin.Context) {
	var flinkJobMeta FlinkJobRequestBody
	err := ginContext.BindJSON(&flinkJobMeta)
	if err != nil {
		logger.GetLog().Errorf("ginContext BindJSON error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	req, err := http.NewRequest(http.MethodGet, "http://" + flinkJobMeta.ArtifactId + "-rest.flink.svc.cluster.local:8081/jobs/overview", nil)
	if err != nil {
		logger.GetLog().Errorf("http NewRequest error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		logger.GetLog().Errorf("http Call error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	resBody, err := ioutil.ReadAll(res.Body)
	if err != nil {
		logger.GetLog().Errorf("ioutil ReadAll error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	ginContext.Data(http.StatusOK, gin.MIMEJSON, resBody)
}

func DeleteFlinkJob(ginContext *gin.Context) {
	ctx := context.Background()

	clientset, err := kubernetes.GetKubernetesClientSetWithEnv(ctx)
	if err != nil {
		logger.GetLog().Errorf("kubernetes GetKubernetesClientSet error: %v", err)
		panic(result.Fail(http.StatusInternalServerError, err.Error(), nil))
	}

	deletePolicy := metav1.DeletePropagationForeground
	deploymentName := "flink-kusto"
	if err := clientset.AppsV1().Deployments(kubernetes.FlinkNamespace).Delete(ctx, deploymentName, metav1.DeleteOptions{
		PropagationPolicy: &deletePolicy,
	}); err != nil {
		panic(err)
	}

	ginContext.JSON(http.StatusOK, result.Success("delete job success", nil))
}
