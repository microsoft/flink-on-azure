package helm

import (
	"context"
	"fmt"
	"io/ioutil"
	"log"
	"os"

	"helm.sh/helm/v3/pkg/action"
	"helm.sh/helm/v3/pkg/chart"
	"helm.sh/helm/v3/pkg/chart/loader"
	"helm.sh/helm/v3/pkg/release"
	"k8s.io/client-go/tools/clientcmd"
	"sigs.k8s.io/yaml"

	"job-scheduler/core/common"
	"job-scheduler/core/keyvault"
	"job-scheduler/core/logger"
)

func GetActionConfig(clientConfig clientcmd.ClientConfig, releaseNamespace string) (config *action.Configuration, err error) {
	actionConfig := new(action.Configuration)
	restClientGetter := NewRESTClientGetter(releaseNamespace, clientConfig)
	if err := actionConfig.Init(restClientGetter, releaseNamespace, os.Getenv("HELM_DRIVER"), func(format string, v ...interface{}) {
		fmt.Sprintf(format, v)

	}); err != nil {
		logger.GetLog().Errorf("actionConfig Init error: %v", err)
		return nil, err
	}
	return actionConfig, nil
}

func getValuesFile() string {
	env := common.GetEnv()
	values := "values.yaml"
	switch env {
	case common.CI:
		values = "values-ci.yaml"
	case common.INT:
		values = "values.yaml"
	case common.PROD:
		values = "values-prod-cus.yaml"
	default:
		values = "values.yaml"
	}
	return values
}

func GetHelmChart(chartPath string) (*chart.Chart, error) {
	chart, err := loader.Load(chartPath)
	if err != nil {
		logger.GetLog().Errorf("loader Load err %v", err)
		return nil, err
	}

	chart.Values = make(map[string]interface{})

	yamlFile, err := ioutil.ReadFile(chartPath + "/" + getValuesFile())
	if err != nil {
		logger.GetLog().Errorf("ioutil ReadFile err %v ", err)
		return nil, err
	}
	if err := yaml.Unmarshal(yamlFile, &chart.Values); err != nil {
		logger.GetLog().Errorf("yaml Unmarshal %v ", err)
		return nil, err
	}

	return chart, nil
}

func OverwriteEnvADLSGen2AccountKey(chart *chart.Chart) (*chart.Chart, error) {
	envMap := chart.Values["env"].(map[string]interface{})
	log.Print(envMap)
	ctx := context.Background()
	accountKey, err := keyvault.GetSecret(ctx, "XPayADLSGen2AccountKey")
	if err != nil {
		logger.GetLog().Errorf("keyvault GetSecret error: %v", err)
		return nil, err
	}
	envMap["adlsGen2AccountKey"] = accountKey
	chart.Values["env"] = envMap
	return chart, nil
}

func HelmInstall(actionConfig *action.Configuration, chart *chart.Chart, releaseName string) error {
	iCli := action.NewInstall(actionConfig)
	iCli.ReleaseName = releaseName

	rel, err := iCli.Run(chart, nil)
	if err != nil {
		logger.GetLog().Errorf("iCli Run %v ", err)
		return err
	}

	logger.GetLog().Info(rel)
	return nil
}

func HelmUninstall(actionConfig *action.Configuration, releaseName string) error {
	iCli := action.NewUninstall(actionConfig)
	_, err := iCli.Run(releaseName)
	if err != nil {
		logger.GetLog().Errorf("iCli Run %v ", err)
		return err
	}
	return nil
}

func HelmList(actionConfig *action.Configuration) ([]*release.Release, error) {
	iCli := action.NewList(actionConfig)
	releases, err := iCli.Run()
	if err != nil {
		logger.GetLog().Errorf("iCli Run %v ", err)
		return nil, err
	}
	return releases, nil
}
