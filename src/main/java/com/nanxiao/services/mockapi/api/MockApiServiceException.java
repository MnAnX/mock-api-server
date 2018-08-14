package com.nanxiao.services.mockapi.api;

public class MockApiServiceException extends Exception {
    public MockApiServiceException(String error) {
        super(error);
    }

    public MockApiServiceException(String error, Exception e) {
        super(error, e);
    }
}
