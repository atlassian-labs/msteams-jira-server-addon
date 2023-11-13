package com.microsoft.teams.service.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.microsoft.teams.service.HttpClientService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.CommandMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import com.microsoft.teams.service.HostPropertiesService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandMessageHandler implements ProcessMessageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(CommandMessageHandler.class);

    private final TeamsAtlasUserServiceImpl userService;
    private final ImageHelper imageHelper;
    private final HostPropertiesService hostProperties;
    private final HttpClientService httpClientService;
    private final RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(5000)
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .build();

    // Should match parameter sent by JiraServerCommandRequest in JiraServerAuthService.DoLogout
    private final String LOGOUT_COMMAND_NAME = "Logout";

    @Autowired
    public CommandMessageHandler(TeamsAtlasUserServiceImpl userService,
                                 ImageHelper imageHelper,
                                 HostPropertiesService hostProperties,
                                 HttpClientService httpClientService) {
        this.userService = userService;
        this.imageHelper = imageHelper;
        this.hostProperties = hostProperties;
        this.httpClientService = httpClientService;
    }

    @Override
    public String processMessage(TeamsMessage teamsMessage) {
        int code = 200;
        String message = StringUtils.EMPTY;

        CommandMessage cmdMessage = (CommandMessage) teamsMessage;
        LOG.info("Command received: {}", cmdMessage.getCommand());
        if (LOGOUT_COMMAND_NAME.equals(cmdMessage.getCommand())) {
            String teamsId = cmdMessage.getTeamsId();
            userService.deleteAoObject(teamsId);
            message = String.format("User %s has been successfully deleted", teamsId);
            performUserLogout();
        }

        return new ResponseMessage(imageHelper)
                .withCode(code)
                .withMessage(message)
                .build();
    }

    void performUserLogout() {
        HttpClient client = httpClientService.getClient();
        String dashboardUrl = getDashboardUrlForProduct();
        String loginPage = getPageContent(dashboardUrl, client);
        String logoutAtlToken = StringUtils.substringBetween(loginPage, "href=\"/jira/logout?", "\">Log Out");
        LOG.debug("\r\n\r\nLogout token:{}\r\n\r\n", logoutAtlToken);
        sendGet(String.format("%s/jira/logout?%s", hostProperties.getBaseUrl(), logoutAtlToken), client);
    }

    private static final String JIRA_PRODUCT_NAME = "JIRA";
    private static final String JIRA_DASHBOARD_URL = "%s/secure/Dashboard.jspa";
    private static final String USER_AGENT = "Mozilla/5.0";

    private void sendGet(String url, HttpClient client) {
        try {
            HttpGet request = new HttpGet(url);
            request.addHeader("User-Agent", USER_AGENT);
            request.setConfig(requestConfig);
            client.execute(request);
            request.releaseConnection();
        } catch (IOException e) {
            LOG.debug(e.getMessage());
        }
    }

    private String getDashboardUrlForProduct() {
        String url = "";
        if (JIRA_PRODUCT_NAME.equals(hostProperties.getDisplayName())) {
            url = String.format(JIRA_DASHBOARD_URL, hostProperties.getFullBaseUrl());
        }
        return url;
    }

    private String getPageContent(String url, HttpClient client) {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");
        request.setConfig(requestConfig);
        StringBuilder result = new StringBuilder();
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            EntityUtils.consume(entity);
            request.releaseConnection();
        } catch (IOException e) {
            LOG.debug(e.getMessage());
        }
        return result.toString();
    }

}
