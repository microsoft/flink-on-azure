package common

import (
	"os"
	"strings"
)

const (
	CI   = "ci"
	INT  = "int"
	PROD = "prod"

	APPLICATION = "APPLICATION"
	ENVIRONMENT = "ENVIRONMENT"
	REGION      = "REGION_NAME"
)

func GetEnv() string {
	environment := strings.ToLower(os.Getenv(ENVIRONMENT))
	if environment == "" {
		environment = CI
	}
	return environment
}
