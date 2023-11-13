package com.microsoft.teams.service.messaging;

import com.microsoft.teams.oauth.AtlasOAuthClient;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.KeysService;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.models.AuthParamMessage;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;
import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
public class AuthParamMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(AuthParamMessageHandler.class);

    private static final String AUTH_FAILED_MSG = "Authorization failed.";
    private static final String AUTH_LINK_FAILED_MSG = "Failed to generate authorization link. Please contact your Jira Server administrator and confirm Application link for Microsoft Teams app has been properly configured.";
    private static final String TOKEN_REJECTED = "token_rejected";
    private static final String TOKEN_REJECTED_MSG = "OAuth token rejected. Please check if verification code is correct and try to resubmit it or re-authorize again to get new code.";
    private static final String PERMISSION_UNKNOWN = "permission_unknown";
    private static final String PERMISSION_UNKNOWN_MSG = "Unknown permissions. Please attempt to re-authorize again.";

    private final PropertiesClient propertiesClient;
    private final AtlasOAuthClient atlasOAuthClient;
    private final ImageHelper imageHelper;
    private final KeysService keysService;
    private final AppKeysService appKeysService;
    private final RequestService requestService;

    @Autowired
    public AuthParamMessageHandler(PropertiesClient propertiesClient,
                                   AtlasOAuthClient atlasOAuthClient,
                                   ImageHelper imageHelper,
                                   KeysService keysService,
                                   AppKeysService appKeysService,
                                   RequestService requestService) {
        this.propertiesClient = propertiesClient;
        this.atlasOAuthClient = atlasOAuthClient;
        this.imageHelper = imageHelper;
        this.keysService = keysService;
        this.appKeysService = appKeysService;
        this.requestService = requestService;
    }

    @Override
    public String processMessage(TeamsMessage message) {
        String teamsId = message.getTeamsId();
        String msg;
        int code = HttpStatus.SC_OK;

        AuthParamMessage authMessage = (AuthParamMessage) message;
        String requestToken = authMessage.requestToken;
        if (!StringUtils.isEmpty(authMessage.verificationCode)) {
            Map<String, String> properties = new HashMap<>();
            try {
                String accessToken = atlasOAuthClient.getAccessToken(requestToken, authMessage.verificationCode, keysService.getConsumerKey(), keysService.getPrivateKey());
                LOG.info("Access token is: {}", accessToken);
                properties.put(TEAMS_ID, teamsId);
                properties.put(ACCESS_TOKEN, accessToken);
                propertiesClient.saveUserToDatabase(properties);
                properties.put(USER_NAME, requestUserName(teamsId, accessToken));
                propertiesClient.saveUserToDatabase(properties);
                msg = String.format("User with the name: %s has been created", teamsId);
            } catch (Exception e) {
                code = HttpStatus.SC_BAD_REQUEST;
                msg = getMessageForException(e.getMessage());
                exceptionLogExtender("getAccessToken error ", Level.DEBUG, e);
            }
        } else {
            try {
                msg = atlasOAuthClient.getAndAuthorizeTemporaryTokenWithUrl(keysService.getConsumerKey(), keysService.getPrivateKey());
            } catch (Exception e) {
                code = HttpStatus.SC_BAD_REQUEST;
                msg = AUTH_LINK_FAILED_MSG;
                exceptionLogExtender("getAndAuthorizeTemporaryTokenWithUrl error ", Level.DEBUG, e);
            }
        }

        return new ResponseMessage(imageHelper).withCode(code).withMessage(msg).build();
    }

    private String requestUserName(String teamsId, String accessToken) {
        final RequestMessage requestMessage = new RequestMessage();
        requestMessage.setAtlasId(appKeysService.get().get("atlas_id"));
        requestMessage.setTeamsId(teamsId);
        requestMessage.setRequestUrl(("api/2/myself"));
        requestMessage.setToken(accessToken);
        requestMessage.setRequestType("GET");
        final String atlasData = requestService.getAtlasData(requestMessage);

        return StringUtils.substringBetween(atlasData, "name\":\"", "\"");
    }

    private String getMessageForException(String exceptionMsg) {
        if (exceptionMsg.contains(TOKEN_REJECTED)) {
            return TOKEN_REJECTED_MSG;
        }
        if (exceptionMsg.contains(PERMISSION_UNKNOWN)) {
            return PERMISSION_UNKNOWN_MSG;
        }

        return AUTH_FAILED_MSG;
    }
}
