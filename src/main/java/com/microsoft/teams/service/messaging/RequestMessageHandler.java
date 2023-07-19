package com.microsoft.teams.service.messaging;

import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(RequestMessageHandler.class);

    private final TeamsAtlasUserServiceImpl userService;
    private final RequestService requestService;
    private final ImageHelper imageHelper;

    @Autowired
    public RequestMessageHandler(TeamsAtlasUserServiceImpl userService,
                                 RequestService requestService,
                                 ImageHelper imageHelper) {
        this.userService = userService;
        this.requestService = requestService;
        this.imageHelper = imageHelper;
    }

    @Override
    public String processMessage(TeamsMessage message) {
        String response;
        String teamsId = message.getTeamsId();
        List<TeamsAtlasUser> userByTeamsId = userService.getUserByTeamsId(teamsId);
        if (!userByTeamsId.isEmpty()) {
            response = requestService.getAtlasData((RequestMessage) message);
        } else {
            response = new ResponseMessage(imageHelper)
                    .withCode(401)
                    .withMessage(String.format("User %s is not authenticated", teamsId))
                    .build();
        }
        return response;
    }

}
