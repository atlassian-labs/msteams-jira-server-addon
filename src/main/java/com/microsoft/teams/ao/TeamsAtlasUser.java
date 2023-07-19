package com.microsoft.teams.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Indexed;

import java.util.Date;

public interface TeamsAtlasUser extends Entity {

    @Indexed
    String getMsTeamsUserId();
    void setMsTeamsUserId(String msTeamsUserId);

    String getAtlasAccessToken();
    void setAtlasAccessToken(String atlasAccessToken);

    Date getDateCreated();
    void setDateCreated(Date dateCreated);

    Date getDateUpdated();
    void setDateUpdated(Date dateUpdated);

    String getUserName();
    void setUserName(String userName);
}
