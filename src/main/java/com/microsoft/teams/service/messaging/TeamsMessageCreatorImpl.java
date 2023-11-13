package com.microsoft.teams.service.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.teams.service.AnnotatedDeserializer;
import com.microsoft.teams.service.models.CommandMessage;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.service.models.AuthParamMessage;
import org.jose4j.jwt.GeneralJwtException;
import org.springframework.stereotype.Component;

@Component
public class TeamsMessageCreatorImpl implements TeamsMessageCreator {

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(RequestMessage.class, new AnnotatedDeserializer<RequestMessage>())
            .registerTypeAdapter(AuthParamMessage.class, new AnnotatedDeserializer<AuthParamMessage>())
            .create();

    public TeamsMessage create(String teamsMsg) throws GeneralJwtException {
        if (teamsMsg.contains("verificationCode")) {
            return gson.fromJson(teamsMsg, AuthParamMessage.class);
        } else if (teamsMsg.contains("request")) {
            return gson.fromJson(teamsMsg, RequestMessage.class);
        } else if (teamsMsg.contains("command")) {
            return gson.fromJson(teamsMsg, CommandMessage.class);
        } else {
            throw new GeneralJwtException("Invalid parameters in JSON message");
        }
    }
}
