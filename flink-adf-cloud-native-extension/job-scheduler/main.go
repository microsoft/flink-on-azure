package main

import (
	"job-scheduler/config"
	"job-scheduler/core/logger"
	"job-scheduler/routers"
)

func main() {
	config.Init()
	logger.Init()
	config := config.GetConfig()
	r := routers.NewRouter()
	// Listen and Server in 0.0.0.0:8080
	r.Run(":" + config.GetString("server.port"))
}
