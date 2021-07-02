package com.microsoft.teams.service;

import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.teams.oauth.AtlasOAuthClient;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.utils.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import static com.google.api.client.http.HttpMethods.*;
import static com.microsoft.teams.oauth.PropertiesClient.*;

@Component
public class RequestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequestService.class);

    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final int UNEXPECTED_ERROR_CODE = 500;

    private static final String REQUEST_URL_PATTERN = "%s/rest/%s";
    private static final String CONSENT_WAS_REVOKED = "Consent token was revoked";

    private final PropertiesClient propertiesClient;
    private final AtlasOAuthClient atlasOAuthClient;
    private final ImageHelper imageHelper;
    private final KeysService keysService;
    private final HostPropertiesService hostProperties;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    public RequestService(PropertiesClient propertiesClient,
                          AtlasOAuthClient atlasOAuthClient,
                          ImageHelper imageHelper,
                          KeysService keysService,
                          HostPropertiesService hostProperties) {
        this.propertiesClient = propertiesClient;
        this.atlasOAuthClient = atlasOAuthClient;
        this.imageHelper = imageHelper;
        this.keysService = keysService;
        this.hostProperties = hostProperties;
    }

    public String getAtlasData(RequestMessage requestMsg) {
        if (POST.equals(requestMsg.getRequestType())) {
            return handlePostRequest(requestMsg);
        } else if (GET.equals(requestMsg.getRequestType())) {
            return handleGetRequest(requestMsg);
        } else if (PUT.equals(requestMsg.getRequestType())) {
            return handlePutRequest(requestMsg);
        } else if (DELETE.equals(requestMsg.getRequestType())) {
            return handleDeleteRequest(requestMsg);
        } else {
            return new ResponseMessage()
                    .withMessage("Unknown request type")
                    .build();
        }
    }

    private OAuthParameters getOAuthParametersForRequest(String teamsId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Map<String, String> properties = propertiesClient.getPropertiesFromDb(teamsId);
        String tmpToken = properties.get(ACCESS_TOKEN);
        String secret = properties.get(SECRET);
        String consumerKey = keysService.getConsumerKey();
        String privateKey = keysService.getPrivateKey();
        return atlasOAuthClient.getParameters(tmpToken, secret, consumerKey, privateKey);
    }

    public HttpRequestFactory getHttpRequestFactory(String teamsId) {
        OAuthParameters parameters = null;
        try {
            parameters = getOAuthParametersForRequest(teamsId);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.debug(e.getMessage());
        }
        return new NetHttpTransport().createRequestFactory(parameters);
    }

    /**
     * Makes request to Atlassian product to provided url and prints response content
     *
     * @param requestMessage RequestMessage object from MsTeams request message
     * @return response from Atlassian product
     */
    private String handleGetRequest(RequestMessage requestMessage) {
        String atlasResponse = StringUtils.EMPTY;
        int code = SUCCESS_RESPONSE_CODE;
        String msg = StringUtils.EMPTY;
        HttpRequestFactory requestFactory = getHttpRequestFactory(requestMessage.getTeamsId());
        try {
            HttpResponse response = requestFactory.buildGetRequest(new GenericUrl(
                    String.format(REQUEST_URL_PATTERN, hostProperties.getFullBaseUrl(), requestMessage.getRequestUrl())))
                    .execute();
            try {
                atlasResponse = response.parseAsString();
            } finally {
                response.disconnect();
            }
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 401){
                code = 401;
                msg = CONSENT_WAS_REVOKED;
            } else {
                code = e.getStatusCode();
                msg = ExceptionHelpers.getResponseErrorMessage(e.getContent());
            }
        } catch (IOException e) {
            LOG.debug(e.getMessage());
            code = UNEXPECTED_ERROR_CODE;
            msg = e.getMessage();
        }
        return new ResponseMessage(imageHelper)
                .withCode(code)
                .withResponse(atlasResponse)
                .withMessage(msg)
                .build(hostProperties.getFullBaseUrl(), requestFactory);
    }

    private String handlePostRequest(RequestMessage requestMessage) {
        String atlasResponse = StringUtils.EMPTY;
        int code = SUCCESS_RESPONSE_CODE;
        String msg = StringUtils.EMPTY;
        HttpRequestFactory requestFactory = getHttpRequestFactory(requestMessage.getTeamsId());
        Object jsonObject;
        HttpResponse response = null;
        try {
            if (requestMessage.getRequestBody().isEmpty()) {
                jsonObject = gson.fromJson("{}", Object.class);
            } else {
                jsonObject = gson.fromJson(requestMessage.getRequestBody(), Object.class);
            }
            HttpContent content = new JsonHttpContent(new JacksonFactory(), jsonObject);
            response = requestFactory.buildPostRequest(new GenericUrl(
                    String.format(REQUEST_URL_PATTERN, hostProperties.getFullBaseUrl(), requestMessage.getRequestUrl())), content)
                    .execute();
            atlasResponse = response.parseAsString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 401){
                code = 401;
                msg = CONSENT_WAS_REVOKED;
            } else {
                code = e.getStatusCode();
                msg = ExceptionHelpers.getResponseErrorMessage(e.getContent());
            }
        } catch (IOException e) {
            LOG.debug("Exception ({}) : {}", e.getClass().getCanonicalName(), e.getMessage());
            if ("401".equals(e.getMessage())) {
                code = 401;
                msg = CONSENT_WAS_REVOKED;
            } else {
                code = UNEXPECTED_ERROR_CODE;
                msg = e.getMessage();
            }
        } finally {
            try {
                if (response != null)
                    response.disconnect();
            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
        return new ResponseMessage(imageHelper)
                .withCode(code)
                .withResponse(atlasResponse)
                .withMessage(msg)
                .build(hostProperties.getFullBaseUrl(), requestFactory);
    }

    private String handlePutRequest(RequestMessage requestMessage) {
        String atlasResponse = StringUtils.EMPTY;
        int code = SUCCESS_RESPONSE_CODE;
        String msg = StringUtils.EMPTY;
        HttpRequestFactory requestFactory = getHttpRequestFactory(requestMessage.getTeamsId());
        Object jsonObject;
        HttpResponse response = null;
        try {
            if (requestMessage.getRequestBody().isEmpty()) {
                jsonObject = gson.fromJson("{}", Object.class);
            } else {
                jsonObject = gson.fromJson(requestMessage.getRequestBody(), Object.class);
            }
            HttpContent content = new JsonHttpContent(new JacksonFactory(), jsonObject);
            response = requestFactory.buildPutRequest(new GenericUrl(
                    String.format(REQUEST_URL_PATTERN, hostProperties.getFullBaseUrl(), requestMessage.getRequestUrl())), content)
                    .execute();
            atlasResponse = response.parseAsString();
        } catch (HttpResponseException e) {
            code = e.getStatusCode();
            msg = ExceptionHelpers.getResponseErrorMessage(e.getContent());
        } catch (IOException e) {
            LOG.debug(e.getMessage());
            code = UNEXPECTED_ERROR_CODE;
            msg = e.getMessage();
        } finally {
            try {
                if (response != null)
                    response.disconnect();
            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
        return new ResponseMessage(imageHelper)
                .withCode(code)
                .withResponse(atlasResponse)
                .withMessage(msg)
                .build(hostProperties.getFullBaseUrl(), requestFactory);
    }
    
    private String handleDeleteRequest(RequestMessage requestMessage) {
        String atlasResponse = StringUtils.EMPTY;
        int code = SUCCESS_RESPONSE_CODE;
        String msg = StringUtils.EMPTY;
        HttpRequestFactory requestFactory = getHttpRequestFactory(requestMessage.getTeamsId());
        try {
            HttpResponse response = requestFactory.buildDeleteRequest(new GenericUrl(
                    String.format(REQUEST_URL_PATTERN, hostProperties.getFullBaseUrl(), requestMessage.getRequestUrl())))
                    .execute();
            try {
                atlasResponse = response.parseAsString();
            } finally {
                response.disconnect();
            }
        } catch (HttpResponseException e) {
            code = e.getStatusCode();
            msg = ExceptionHelpers.getResponseErrorMessage(e.getContent());
        } catch (IOException e) {
            LOG.debug(e.getMessage());
            code = UNEXPECTED_ERROR_CODE;
            msg = e.getMessage();
        }
        return new ResponseMessage(imageHelper)
                .withCode(code)
                .withResponse(atlasResponse)
                .withMessage(msg)
                .build(hostProperties.getFullBaseUrl(), requestFactory);
    }

}
