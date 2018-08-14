package com.nanxiao.services.mockapi.api;

import lombok.Data;

/**
 * Created by nanxiao on 9/12/16.
 */
@Data
public class ErrorInfo {
    private long code = 0;
    private String summary;
    private String description;

    public ErrorInfo(long code) {
        this.code = code;
    }

    public boolean hasError() {
        return code != 0;
    }

    public ErrorInfo setSummary(String summary){
        this.summary = summary;
        return this;
    }

    public ErrorInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public ErrorInfo newInstance(){
        return new ErrorInfo(this.code).setSummary(this.summary);
    }
}
