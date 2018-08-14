package com.nanxiao.services.mockapi.api;

import lombok.Data;

/**
 * Created by nanxiao on 8/11/16.
 */
@Data
public class RequestWrap<T> {
    private String uri;
    private String ID;
    private T request;
}
