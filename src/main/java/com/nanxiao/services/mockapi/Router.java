package com.nanxiao.services.mockapi;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanxiao.services.mockapi.api.*;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by nan on 10/1/2016.
 */
@Singleton
@Slf4j
public class Router {
    private Map<String, Function<HttpRequest, String>> apiMap = Maps.newHashMap();
    private final Gson gson;

    @Inject
    public Router() {
        this.gson = new GsonBuilder().create();
        initRouting();
    }

    public Map<String, Function<HttpRequest, String>> getApiMap() {
        return apiMap;
    }

    public String addApi(String path, String resp) {
        String uri = "/" + path;
        apiMap.put(uri, request -> resp);
        return uri;
    }

    public boolean existsApi(String path) {
        String uri = "/" + path;
        return apiMap.get(uri) != null;
    }

    private void initRouting() {
        apiMap.put("/health-check", request -> "ok");
        apiMap.put("/mock", request -> handlePost(request, r -> mockApi(r)));
    }

    private ResponseWrap<MockApiResp> mockApi(RequestWrap<String> requestWrap) {
        return process(requestWrap,
            MockApiReq.class,
            ErrorEnum.MockApi,
            req -> {
                if (req.getPath() == null) {
                    return "Mock path is required.";
                }
                if (req.getMockResponse() == null) {
                    return "Mock response is required.";
                }
                return null;
            },
            req -> {
                MockApiResp ret = new MockApiResp();

                if(existsApi(req.getPath())) {
                    ret.setError("Api path ["+ req.getPath() +"] already exists");
                } else {
                    // add mock api
                    addApi(req.getPath(), req.getMockResponse());
                    // construct mock url. TODO: generate short url
                    String url = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/" + req.getPath();
                    log.info("Mock path ["+ req.getPath() +"] has been added, generated url: " + url);

                    ret.setUrl(url);
                    ret.setMockResponse(req.getMockResponse());
                }

                return ret;
            });
    }

    // Request parser

    private String handlePost(HttpRequest request, Function<RequestWrap, ResponseWrap> function) {
        String response;
        try {
            RequestWrap<String> req = getPostAttributes(request);
            log.debug("Request to [{}]: {}", request.uri(), req.toString());
            ResponseWrap resp = function.apply(req);
            resp.setID(req.getID());
            response = gson.toJson(resp);
            log.trace("Response of [{}]: {}", request.uri(), response);
        } catch (InvalidRequestException e) {
            response = createInvalidRequestResponse("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            response = createInvalidRequestResponse("Internal error" + e.getMessage());
        }
        return response;
    }

    private String createInvalidRequestResponse(String error) {
        ResponseWrap resp = new ResponseWrap();
        resp.setError(ErrorEnum.InvalidRequest.getError().setDescription(error));
        return gson.toJson(resp);
    }

    private RequestWrap<String> getPostAttributes(HttpRequest request) throws InvalidRequestException {
        RequestWrap<String> wrap = new RequestWrap<>();
        wrap.setUri(request.uri());
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
        try {
            // Parse request ID
            InterfaceHttpData id = decoder.getBodyHttpData("id");
            if (id != null) {
                if (id.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) id;
                    try {
                        wrap.setID(attribute.getValue());
                    } catch (IOException e) {
                        throw new InvalidRequestException("Invalid request ID " + request.uri() + ": " + request.toString());
                    }
                } else {
                    throw new InvalidRequestException("Invalid request ID type: " + id.getHttpDataType());
                }
            }
            // Parse request body
            InterfaceHttpData data = decoder.getBodyHttpData("req");
            if (data == null) {
                throw new InvalidRequestException("Empty request body.");
            }
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                try {
                    wrap.setRequest(attribute.getValue());
                } catch (IOException e) {
                    throw new InvalidRequestException("Invalid request body of " + request.uri() + ": " + request.toString());
                }
            } else {
                throw new InvalidRequestException("Invalid request type: " + data.getHttpDataType());
            }
            // Parse file upload
            InterfaceHttpData file = decoder.getBodyHttpData("file");
            if (file != null) {
                if (file.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) file;
                    try {
                        // Ignore
                    } catch (Exception e) {
                        log.info("Invalid file upload of " + request.uri() + ": " + e.getMessage(), e);
                        throw new InvalidRequestException("Invalid file upload of " + request.uri() + ": " + e.getMessage());
                    }
                } else {
                    throw new InvalidRequestException("Invalid file upload type: " + file.getHttpDataType());
                }
            }
            return wrap;
        } finally {
            decoder.destroy();
        }
    }

    class InvalidRequestException extends Exception {
        public InvalidRequestException(String e) {
            super(e);
        }
    }

    // Helpers

    private <I, O> ResponseWrap<O> process(RequestWrap<String> requestWrap,
                                           Class<I> requestType,
                                           ErrorEnum errorEnum,
                                           CustomFunction<I, String> validateFn,
                                           CustomFunction<I, O> processRequestFn) {
        ResponseWrap<O> resp = new ResponseWrap<>();
        I req;
        // Parse request
        try {
            req = gson.fromJson(requestWrap.getRequest(), requestType);
        } catch (Exception e) {
            resp.setError(errorEnum.getError().setDescription("Invalid request format: " + e.getMessage()));
            return resp;
        }
        if (req == null) {
            resp.setError(errorEnum.getError().setDescription("Invalid empty request."));
            return resp;
        }
        // Validate request
        try {
            String err = validateFn.apply(req);
            if (err != null && !err.isEmpty()) {
                resp.setError(errorEnum.getError().setDescription(err));
                return resp;
            }
        } catch (Exception e) {
            resp.setError(errorEnum.getError().setDescription("Request validation error: " + e.getMessage()));
            return resp;
        }
        // Process request
        try {
            O ret = processRequestFn.apply(req);
            resp.setResponse(ret);
        } catch (Exception e) {
            String error = "Failed to process: " + e.getMessage();
            log.error(error, e);
            resp.setError(errorEnum.getError().setDescription(error));
        }
        return resp;
    }

    @FunctionalInterface
    public interface CustomFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
