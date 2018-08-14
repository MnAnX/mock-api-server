package com.nanxiao.services.mockapi;

import com.advicecoach.server.netty.http.responder.HttpResponder;
import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;
import java.util.function.Function;

public final class MockApiServiceResponder extends HttpResponder {
    private final Map<String, Function<HttpRequest, String>> apiMap;

    @Inject
    public MockApiServiceResponder(final Router router) {
        apiMap = router.getApiMap();
    }

    @Override
    protected boolean haveMatchingResource(HttpRequest request) {
        return true;
    }

    @Override
    protected boolean isSupportedMethod(HttpRequest request) {
        return apiMap.containsKey(request.uri());
    }

    @Override
    protected FullHttpResponse generateResponse(HttpRequest request) {
        try {
            String response = apiMap.get(request.uri()).apply(request);
            return createSuccessResponse(response);
        } catch (Exception e) {
            return createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

