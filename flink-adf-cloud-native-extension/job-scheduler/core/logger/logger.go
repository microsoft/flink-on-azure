package logger

import (
	"os"

	"go.elastic.co/ecszap"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"

	"job-scheduler/core/common"
)

var log *zap.Logger

func Init() {
	encoderConfig := ecszap.NewDefaultEncoderConfig()
	encoderConfig.EncodeLevel = zapcore.CapitalLevelEncoder
	core := ecszap.NewCore(encoderConfig, os.Stdout, zap.DebugLevel)
	log = zap.New(core, zap.AddCaller())
	log = log.With(zap.String("application", os.Getenv(common.APPLICATION)))
	log = log.With(zap.String("environment", os.Getenv(common.ENVIRONMENT)+"@"+os.Getenv(common.REGION)))
}

func GetLog() *zap.SugaredLogger {
	return log.Sugar()
}
