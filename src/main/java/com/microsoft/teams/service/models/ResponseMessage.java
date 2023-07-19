package com.microsoft.teams.service.models;

import com.google.api.client.http.HttpRequestFactory;
import com.microsoft.teams.utils.ImageHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResponseMessage {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseMessage.class);

    private Integer code;
    private String response;
    private String message;
    private static final String RESPONSE_MESSAGE = "{\"code\":%d,\"response\":%s,\"message\":\"%s\"}";
    private ImageHelper imageHelper;

    public ResponseMessage() {
        this(null);        
    }

    public ResponseMessage(ImageHelper imageHelper) {
        this.imageHelper = imageHelper;
        this.code = 400;
        this.response = "\"\"";
    }

    public ResponseMessage withCode(Integer code) {
        this.code = code;
        return this;
    }

    public ResponseMessage withResponse(String response) {
        this.response = StringUtils.EMPTY.equals(response) ? "\"\"" : response;
        return this;
    }

    public ResponseMessage withMessage(String message) {
        this.message = message;
        return this;
    }

    public String build() {
        return String.format(RESPONSE_MESSAGE, code, response, message);
    }

    public String build(String baseUrl, HttpRequestFactory factory) {
        String result = imageHelper == null
                ? "\"\""
                : imageHelper.replaceImagesInJson(response, baseUrl, factory);
        return String.format(RESPONSE_MESSAGE, code, result, message);
    }
}
