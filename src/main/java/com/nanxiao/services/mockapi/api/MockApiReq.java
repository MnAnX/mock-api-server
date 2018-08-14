package com.nanxiao.services.mockapi.api;

import lombok.Data;

@Data
public class MockApiReq {
    String path;
    String mockResponse;
}
