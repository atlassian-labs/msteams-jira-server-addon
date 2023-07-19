package com.microsoft.teams.service;

import com.microsoft.teams.service.messaging.*;
import com.microsoft.teams.service.models.*;
import com.microsoft.teams.utils.ImageHelper;
import org.jose4j.jwt.GeneralJwtException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    private static final String SEARCH_PROJECT_LIST_ENDPOINT = "api/2/projects/picker";
    public static final String INVALID_JWT_TOKEN = "Invalid JWT token";

    private final AzureActiveDirectoryService azureAdService;
    private ProcessMessageStrategy strategy;

    private final SearchProjectsMessageHandler searchProjectMessage;
    private final RequestMessageHandler requestMessage;
    private final CommandMessageHandler commandMessage;
    private final ImageHelper imageHelper;
    private final TeamsMessageCreator teamsMessageCreator;
    private final AuthParamMessageHandler authParamMessageHandler;

    @Autowired
    public MessageService(AzureActiveDirectoryService azureAdService,
                          SearchProjectsMessageHandler searchProjectMessage,
                          RequestMessageHandler requestMessage,
                          AuthParamMessageHandler authParamMessageHandler,
                          CommandMessageHandler commandMessage,
                          ImageHelper imageHelper,
                          TeamsMessageCreator teamsMessageCreator) {
        this.azureAdService = azureAdService;
        this.searchProjectMessage = searchProjectMessage;
        this.requestMessage = requestMessage;
        this.commandMessage = commandMessage;
        this.imageHelper = imageHelper;
        this.authParamMessageHandler = authParamMessageHandler;
        this.teamsMessageCreator = teamsMessageCreator;
    }

    String processTeamsMsg(String teamsMessageJson) {
        TeamsMessage teamsMsgObj = null;
        String msg = "Unknown error has occurred";
        try {
            teamsMsgObj = validateTeamsMessage(teamsMessageJson);
        } catch (GeneralJwtException e) {
            LOG.error(e.getMessage());
            msg = e.getMessage();
        }
        if (teamsMsgObj != null) {
            setProcessMessageStrategy(teamsMsgObj);
            return strategy.processMessage(teamsMsgObj);
        }
        int code = 400;
        ResponseMessage responseMessage = new ResponseMessage(imageHelper).withCode(code).withMessage(msg);
        return responseMessage.build();
    }

    private void setProcessMessageStrategy(TeamsMessage teamsMsgObj) {
    	if (teamsMsgObj instanceof AuthParamMessage) {
        	strategy = authParamMessageHandler;
        } else if (teamsMsgObj instanceof RequestMessage) {
        	if (((RequestMessage) teamsMsgObj).getRequestUrl().contains(SEARCH_PROJECT_LIST_ENDPOINT)) {
                strategy = searchProjectMessage;
            } else {
                strategy = requestMessage;
            }
        } else if (teamsMsgObj instanceof CommandMessage) {
            strategy = commandMessage;
        }
    }

    private TeamsMessage validateTeamsMessage(String teamsMessageJson) throws GeneralJwtException {
        if (isJson(teamsMessageJson)) {
            TeamsMessage teamsMsgObj = teamsMessageCreator.create(teamsMessageJson);
            if (isValidBearerToken(teamsMsgObj)) {
                LOG.info("Teams message is valid");
                return teamsMsgObj;
            }
            throw new GeneralJwtException(INVALID_JWT_TOKEN);
        }
        throw new GeneralJwtException("Teams message is invalid JSON");
    }

    private boolean isJson(String teamsMsg) {
        try {
            new JSONObject(teamsMsg);
        } catch (org.json.JSONException ex) {
            try {
                new JSONArray(teamsMsg);
            } catch (org.json.JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidBearerToken(TeamsMessage teamsMsgObj) {
        return azureAdService.isValidToken(teamsMsgObj.getToken(), teamsMsgObj.getTeamsId());
    }
}
