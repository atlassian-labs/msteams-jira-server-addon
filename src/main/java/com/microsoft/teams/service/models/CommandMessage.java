package com.microsoft.teams.service.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.teams.anotations.JsonRequired;

public class CommandMessage implements TeamsMessage {

    @SerializedName("command")
    @Expose
    @JsonRequired
    private String command;
    @SerializedName("teamsId")
    @Expose
    @JsonRequired
    private String teamsId;
    @SerializedName("atlasId")
    @Expose
    @JsonRequired
    private String atlasId;
    @SerializedName("token")
    @Expose
    @JsonRequired
    private String token;

    public String getCommand() {
        return command;
    }

    public String getTeamsId() {
        return teamsId;
    }

    public String getAtlasId() {
        return atlasId;
    }

    public String getToken() {
        return token;
    }

}
