package config

import (
	"log"

	"github.com/spf13/viper"

	"job-scheduler/core/common"
)

var config *viper.Viper

func Init() {
	var err error
	env := common.GetEnv()
	log.Print(env)
	config = viper.New()
	config.SetConfigType("yaml")
	config.SetConfigName(env)
	config.AddConfigPath("../config/")
	config.AddConfigPath("config/")
	err = config.ReadInConfig()
	if err != nil {
		log.Fatal("error on parsing configuration file")
	}
}

func GetConfig() *viper.Viper {
	return config
}
