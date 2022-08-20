package routers

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"job-scheduler/controllers"
	"job-scheduler/core/handler"
	"job-scheduler/core/result"
)

func NewRouter() *gin.Engine {
	r := gin.Default()

	r.Use(handler.Recover)
	r.Use(handler.Auth)

	r.GET("/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, result.Success("Pong", nil))
	})

	r.GET("/api/keepalive", func(c *gin.Context) {
		c.JSON(http.StatusOK, result.Success("", nil))
	})

	r.GET("/probe/healthcheck", func(c *gin.Context) {
		c.JSON(http.StatusOK, result.Success("", nil))
	})

	r.POST("/api/flink/deployment", controllers.CreateFlinkDeploymentJob)

	r.GET("/api/flink/deployment/status", controllers.GetFlinkDeploymentStatus)

	r.GET("/api/flink/deployment/log", controllers.GetFlinkDeploymentLog)

	r.GET("/api/flink/job/id", controllers.GetFlinkJobID)

	r.POST("/api/flink/job/status", controllers.GetFlinkJobStatus)

	r.DELETE("/api/flink/job", controllers.DeleteFlinkJob)

	return r
}
