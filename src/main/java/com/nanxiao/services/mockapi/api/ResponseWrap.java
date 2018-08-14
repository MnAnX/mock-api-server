package com.nanxiao.services.mockapi.api;

import lombok.Data;

/**
 * Created by nanxiao on 8/11/16.
 */
@Data
public class ResponseWrap<T> {
    private String ID;
    private ErrorInfo error;
    private T response;

    public String getID() {
        return ID;
    }

    public ResponseWrap setID(String ID) {
        this.ID = ID;
        return this;
    }

    public boolean isSuccess() {
        return error == null || !error.hasError();
    }

    public ResponseWrap setError(ErrorInfo error) {
        this.error = error;
        return this;
    }

    public ResponseWrap setResponse(T response) {
        this.response = response;
        return this;
    }
}
