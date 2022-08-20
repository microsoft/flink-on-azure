package keyvault

import (
	"context"
	"errors"
	"fmt"

	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/azure-sdk-for-go/sdk/keyvault/azsecrets"

	"job-scheduler/core/common"
	"job-scheduler/core/logger"
)

func getKeyVaultName() string {
	env := common.GetEnv()
	keyvaultName := "ps2ckvci"
	switch env {
	case common.CI:
		keyvaultName = "ps2ckvci"
	case common.INT:
		keyvaultName = "xpay-kv-int"
	case common.PROD:
		keyvaultName = "xpay-kv-prod"
	default:
		keyvaultName = "ps2ckvci"
	}
	return keyvaultName
}

func GetSecret(ctx context.Context, secretName string) (value string, err error) {
	keyVaultName := getKeyVaultName()
	keyVaultUrl := fmt.Sprintf("https://%s.vault.azure.net/", keyVaultName)

	cred, err := azidentity.NewDefaultAzureCredential(nil)
	if err != nil {
		logger.GetLog().Errorf("azidentity NewDefaultAzureCredential error: %v", err)
		return "", err
	}

	client := azsecrets.NewClient(keyVaultUrl, cred, nil)

	resp, err := client.GetSecret(ctx, secretName, "", nil)
	if err != nil {
		logger.GetLog().Errorf("client GetSecret error: %v", err)
		return "", err
	}

	if resp.SecretBundle.Value == nil {
		logger.GetLog().Errorf("SecretBundle Value null")
		return "", errors.New("SecretBundle Value null")
	}

	return *(resp.SecretBundle.Value), nil
}
