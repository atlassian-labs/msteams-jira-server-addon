package com.microsoft.teams.servlets;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.microsoft.teams.service.AppPropertiesService;
import com.microsoft.teams.service.HostPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEndServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(AuthEndServlet.class);

    private static final String PLUGIN_XSRF_TOKEN = "plugin.xsrf.token";

    private final RedirectHelper redirectHelper;
    private final TemplateRenderer renderer;
    private final HostPropertiesService hostProperties;
    private final AppPropertiesService appProperties;
    private final UserManager userManager;

    @Autowired
    public AuthEndServlet(@ComponentImport TemplateRenderer renderer,
                          RedirectHelper redirectHelper,
                          HostPropertiesService hostProperties,
                          AppPropertiesService appProperties, UserManager userManager) {
        this.renderer = renderer;
        this.redirectHelper = redirectHelper;
        this.hostProperties = hostProperties;
        this.appProperties = appProperties;
        this.userManager = userManager;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (redirectHelper.isUserLoggedIn(request, response)) {
                response.setContentType("text/html;charset=utf-8");

                XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
                String oauthVerifier = request.getParameter("oauth_verifier");
                String token = xsrfTokenGenerator.generateToken(request);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("oauthVerifier", oauthVerifier);
                parameters.put("atlasHome", hostProperties.getFullBaseUrl());
                parameters.put("pluginKey", appProperties.getPluginKey());
                parameters.put("username", userManager.getRemoteUsername(request));
                response.addCookie(new Cookie(PLUGIN_XSRF_TOKEN, token));

                renderer.render("templates/authEnd.vm", parameters, response.getWriter());
            }
        } catch (IOException | RenderingException e) {
            LOG.info(e.getMessage());
        }
    }
}
