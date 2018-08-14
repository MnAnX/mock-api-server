package com.nanxiao.services.mockapi.api;

public enum ErrorEnum {
    InvalidRequest(1000, "Invalid request."),
    MockApi(1001, "Error mocking api"),
    ;


    private ErrorInfo error;

    ErrorEnum(long code, String summary) {
        this.error = new ErrorInfo(code).setSummary(summary);
    }

    public ErrorInfo getError() {
        return error.newInstance();
    }
}
