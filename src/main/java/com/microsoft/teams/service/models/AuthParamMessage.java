package com.microsoft.teams.service.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.teams.anotations.JsonRequired;

public class AuthParamMessage implements TeamsMessage {
	
	@SerializedName("teamsId")
    @Expose
    @JsonRequired
    private String teamsId;
 
    @SerializedName("token")
    @Expose
    @JsonRequired
    private String token;

    @SerializedName("verificationCode")
    @Expose
    public String verificationCode;
    
    @SerializedName("requestToken")
    @Expose
    public String requestToken;
    
	public String getTeamsId() { return teamsId; }
	
	public String getToken() { return token; }
}
