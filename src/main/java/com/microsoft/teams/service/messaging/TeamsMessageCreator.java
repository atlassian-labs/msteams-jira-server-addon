package com.microsoft.teams.service.messaging;

import com.microsoft.teams.service.models.TeamsMessage;
import org.jose4j.jwt.GeneralJwtException;

public interface TeamsMessageCreator {
    TeamsMessage create(String teamsMsg) throws GeneralJwtException;
}
