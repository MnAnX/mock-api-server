package com.nanxiao.services.mockapi.api;

import lombok.Data;

@Data
public class MockApiResp {
    String url;
    String mockResponse;
    String error;
}
