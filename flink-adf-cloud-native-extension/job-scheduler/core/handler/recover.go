package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"job-scheduler/core/result"
)

func Recover(c *gin.Context) {
	defer func() {
		if r := recover(); r != nil {
			if e, ok := r.(*result.Result); ok {
				c.JSON(e.Code, e)
			} else {
				c.JSON(http.StatusInternalServerError, e)
			}
			c.Abort()
		}
	}()
	c.Next()
}

// recover错误，转string
func errorToString(r interface{}) string {
	switch v := r.(type) {
	case error:
		return v.Error()
	default:
		return r.(string)
	}
}
