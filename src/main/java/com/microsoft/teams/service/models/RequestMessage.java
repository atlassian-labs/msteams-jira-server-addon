package com.microsoft.teams.service.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.teams.anotations.JsonRequired;

public class RequestMessage implements TeamsMessage {
    @SerializedName("teamsId")
    @Expose
    @JsonRequired
    private String teamsId;
    @SerializedName("atlasId")
    @Expose
    @JsonRequired
    private String atlasId;
    @SerializedName("requestUrl")
    @Expose
    @JsonRequired
    private String requestUrl;
    @SerializedName("requestType")
    @Expose
    @JsonRequired
    private String requestType;
    @SerializedName("requestBody")
    @Expose
    @JsonRequired
    private String requestBody;
    @SerializedName("token")
    @Expose
    @JsonRequired
    private String token;

    public String getTeamsId() {
        return teamsId;
    }

    public String getAtlasId() {
        return atlasId;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getToken() {
        return token;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setTeamsId(String teamsId) {
        this.teamsId = teamsId;
    }

    public void setAtlasId(String atlasId) {
        this.atlasId = atlasId;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
