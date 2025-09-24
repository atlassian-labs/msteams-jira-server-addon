package com.microsoft.teams.servlets;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.microsoft.teams.service.HostPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Component
public class RedirectHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectHelper.class);

    private final UserManager userManager;
    private final HostPropertiesService hostProperties;
    private final LoginUriProvider loginUriProvider;
    private static final String DASHBOARD_URL = "%s/secure/Dashboard.jspa";

    @Autowired
    private RedirectHelper(@ComponentImport UserManager userManager,
                           @ComponentImport LoginUriProvider loginUriProvider,
                           HostPropertiesService hostProperties) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.hostProperties = hostProperties;
    }

    boolean isUserLoggedInAndAdmin(HttpServletRequest request, HttpServletResponse response) {
        boolean checkResult = false;
        UserProfile remoteUser = userManager.getRemoteUser(request);
        if (remoteUser == null) {
            redirectToLogin(request, response);
        } else if (!userManager.isSystemAdmin(remoteUser.getUserKey())) {
            redirectToDashboard(response);
        } else {
            checkResult = true;
        }
        return checkResult;
    }

    boolean isUserLoggedIn(HttpServletRequest request, HttpServletResponse response) {
        boolean checkResult = false;
        UserProfile remoteUser = userManager.getRemoteUser(request);
        if (remoteUser == null) {
            redirectToLogin(request, response);
        } else {
            checkResult = true;
        }
        return checkResult;
    }

    private void redirectToDashboard(HttpServletResponse response) {
        try {
            response.sendRedirect(String.format(DASHBOARD_URL, hostProperties.getFullBaseUrl()));
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }
    }

    private URI getUri(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder(request.getRequestURL());
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

}
