package com.microsoft.teams.service.messaging;

import com.google.gson.Gson;
import com.google.common.collect.ImmutableMap;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.gson.reflect.TypeToken;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.service.models.Project;
import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.utils.ImageHelper;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
public class GetProjectsMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = Logger.getLogger(GetProjectsMessageHandler.class);

    private static final String GET_PROJECTS_FAILED_MSG = "Cannot get projects.";

    private static final String GET_AVATARS_PARAMETER = "getAvatars";
    private final TeamsAtlasUserServiceImpl userService;
    private final RequestService requestService;
    private final HostPropertiesService hostProperties;
    private final ImageHelper imageHelper;

    @Autowired
    public GetProjectsMessageHandler(TeamsAtlasUserServiceImpl userService,
                                    RequestService requestService,
                                    HostPropertiesService hostProperties,
                                    ImageHelper imageHelper) {
        this.userService = userService;
        this.requestService = requestService;
        this.imageHelper = imageHelper;
        this.hostProperties = hostProperties;
    }

    @Override
    public String processMessage(TeamsMessage message) {
        String response;
        String teamsId = message.getTeamsId();
        List<TeamsAtlasUser> userByTeamsId = userService.getUserByTeamsId(teamsId);
        if (!userByTeamsId.isEmpty()) {
            RequestMessage messageForGettingName = (RequestMessage) message;
            messageForGettingName.setRequestUrl("api/2/myself");
            String responseWithName = requestService.getAtlasData(messageForGettingName);
            response = getProjectsForUserByProjectName(responseWithName, messageForGettingName);
        } else {
            response = new ResponseMessage()
                    .withCode(401)
                    .withMessage(String.format("User %s is not authenticated", teamsId))
                    .build();
        }
        return response;
    }

    private String getProjectsForUserByProjectName(String atlasResponse, RequestMessage message) {
        String msg;
        int code = HttpStatus.SC_OK;
        long startTime = System.currentTimeMillis();

        try {
            List<com.atlassian.jira.project.Project> jiraProjectsList = new ArrayList<>();
            List<Project> projectsList = new ArrayList<>();
            String userName = StringUtils.substringBetween(atlasResponse, "name\":\"", "\"");
            Map requestBodyMap = message.getRequestBody() == null ? null : new Gson().fromJson(message.getRequestBody(), Map.class);
            Object getAvatarsValue = requestBodyMap == null ? null : requestBodyMap.get(GET_AVATARS_PARAMETER);
            Boolean getAvatars = getAvatarsValue == null ? false : Boolean.parseBoolean(getAvatarsValue.toString());
            ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(userName);

            ProjectService projectService = ComponentAccessor.getComponent(ProjectService.class);
            jiraProjectsList = projectService.getAllProjects(user).get();

            long finishTime = System.currentTimeMillis();
            long timeElapsed = finishTime - startTime;
    
            LOG.debug("Received raw projects in " + timeElapsed + " milliseconds. Total number of projects: " + jiraProjectsList.size());

            AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);

            jiraProjectsList.forEach(x -> {
                Project project = new Project();
                project.setId(x.getId().toString());
                project.setKey(x.getKey());
                project.setName(x.getName());

                if (getAvatars) {
                    project.setAvatarUrls(ImmutableMap.of("24x24", avatarService.getProjectAvatarAbsoluteURL(x, Avatar.Size.NORMAL)));
                }

                projectsList.add(project);
            });

            msg = new Gson().toJson(projectsList, new TypeToken<ArrayList<Project>>() {
            }.getType());
        } catch (Exception e) {
            code = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            msg = GET_PROJECTS_FAILED_MSG;
            exceptionLogExtender("getProjectsForUserByProjectName error ", Level.DEBUG, e);
        }

        long finishTime = System.currentTimeMillis();
        long timeElapsed = finishTime - startTime;

        LOG.debug("Processed projects in " + timeElapsed + " milliseconds");

        return new ResponseMessage(imageHelper).withCode(code).withResponse(msg).withMessage("").build(
                hostProperties.getFullBaseUrl(), requestService.getHttpRequestFactory(message.getTeamsId())
        );
    }
}
