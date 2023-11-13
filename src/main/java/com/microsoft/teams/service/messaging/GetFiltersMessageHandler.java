package com.microsoft.teams.service.messaging;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.Gson;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.gson.reflect.TypeToken;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.Filter;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GetFiltersMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(GetFiltersMessageHandler.class);
    private static final int PAGE_POSITION = 0;
    private static final int PAGE_WIDTH = 50;
    private static final String FILTER_NAME_PARAMETER = "filterName";
    private static final String ALL_FILTERS = "";

    private final TeamsAtlasUserServiceImpl userService;
    private final RequestService requestService;
    private final ImageHelper imageHelper;

    @Autowired
    public GetFiltersMessageHandler(TeamsAtlasUserServiceImpl userService,
                                    RequestService requestService,
                                    ImageHelper imageHelper,
                                    @ComponentImport JiraThreadLocalUtil jiraThreadLocalUtil) {
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
            RequestMessage messageForGettingName = (RequestMessage) message;
            messageForGettingName.setRequestUrl("api/2/myself");
            String responseWithName = requestService.getAtlasData(messageForGettingName);
            response = getFiltersForUserByFilterName(responseWithName, messageForGettingName.getRequestBody());
        } else {
            response = new ResponseMessage(imageHelper)
                    .withCode(401)
                    .withMessage(String.format("User %s is not authenticated", teamsId))
                    .build();
        }
        return response;
    }

    private String getFiltersForUserByFilterName(String atlasResponse, String requestBody) {
        List<Filter> filtersList = new ArrayList<>();
        String userName = StringUtils.substringBetween(atlasResponse, "name\":\"", "\"");
        Map requestBodyMap = new Gson().fromJson(requestBody, Map.class);
        String filterName = escapeFilterName((String) requestBodyMap.get(FILTER_NAME_PARAMETER));
        ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(userName);
        JiraServiceContext jsc = new JiraServiceContextImpl(user);
        SharedEntitySearchParameters searchParams = new SharedEntitySearchParametersBuilder()
                .setEntitySearchContext(SharedEntitySearchContext.USE)
                .setSortColumn(SharedEntityColumn.NAME, true)
                .setName(filterName)
                .toSearchParameters();

        SharedEntitySearchResult<SearchRequest> filtersResult;

        long startTime = System.currentTimeMillis();

        try {
            filtersResult = ComponentAccessor.getComponent(SearchRequestService.class)
                    .search(jsc, searchParams, PAGE_POSITION, PAGE_WIDTH);

            long finishTime = System.currentTimeMillis();
            long timeElapsed = finishTime - startTime;

            LOG.debug("Received raw filters in " + timeElapsed + " milliseconds. Total number of filters: " + filtersResult.size());

        } catch (Exception ex) {
            LOG.debug(String.format("Cannot parse requested filter name %s, cause of error(%s): %s",
                    filterName, ex.getClass().getCanonicalName(), ex.getMessage()));

            return new ResponseMessage().withCode(200).withMessage("Filter can't be parsed").build();
        }

        if (filtersResult != null) {
            List<SearchRequest> filters = filtersResult.getResults();
            for (SearchRequest filterFromRequest : filters) {
                Filter filter = new Filter();
                filter.setId(filterFromRequest.getId().toString());
                filter.setName(filterFromRequest.getName());
                filter.setJql(filterFromRequest.getQuery().getQueryString());
                filtersList.add(filter);
            }
        }

        String filtersJSON = new Gson().toJson(filtersList, new TypeToken<ArrayList<Filter>>() {}.getType());

        long finishTime = System.currentTimeMillis();
        long timeElapsed = finishTime - startTime;

        LOG.debug("Received filters in " + timeElapsed + " milliseconds");

        return new ResponseMessage().withCode(200).withResponse(filtersJSON).withMessage("").build();
    }

    private String escapeFilterName(String filterName) {
        if (filterName == null) {
            return ALL_FILTERS;
        }

        String escapedFilterName = filterName
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");


        return "\"" + escapedFilterName + "\"";
    }

}
