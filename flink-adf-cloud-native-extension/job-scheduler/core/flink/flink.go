package flink

import (
	"job-scheduler/core/common"
)

func GetFlinkHistoryServerURL() string {
	env := common.GetEnv()
	flinkHistoryServerURL := "https://flink-history.webxtsvc-int.microsoft.com/"
	switch env {
	case common.CI:
		flinkHistoryServerURL = "https://flink-history.webxtsvc-int.microsoft.com/"
	case common.INT:
		flinkHistoryServerURL = "https://flink-history.webxtsvc-ppe.microsoft.com/"
	case common.PROD:
		flinkHistoryServerURL = "https://flink-history-cus.webxtsvc.microsoft.com/"
	default:
		flinkHistoryServerURL = "https://flink-history.webxtsvc-int.microsoft.com/"
	}
	return flinkHistoryServerURL
}
