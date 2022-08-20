package result

import "net/http"

type Result struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
	Data any    `json:"data"`
}

func Success(msg string, data any) *Result {
	return &Result{
		Code: http.StatusOK,
		Msg:  msg,
		Data: data,
	}
}

func Fail(code int, msg string, data any) *Result {
	return &Result{
		Code: code,
		Msg:  msg,
		Data: data,
	}
}
