package com.microsoft.teams.listener.dto;

public class MsTeamsUserId {

    private String msTeamsUserId;

    public MsTeamsUserId(String msTeamsUserId) {
        this.msTeamsUserId = msTeamsUserId;
    }

    @Override
    public String toString() {
        return "MsTeamsUserId{" +
                "msTeamsUserId='" + msTeamsUserId + '\'' +
                '}';
    }
}
