package com.microsoft.teams.servlets;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.microsoft.teams.config.PluginSettings;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class ConfigPageServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigPageServlet.class);
    private static final String EMBED_ICONS = "embed-icons";
    private static final String EMBED_AVATARS = "embed-avatars";
    private static final String EMBED_PROJECT_AVATARS = "embed-project-avatars";
    private static final String PLUGIN_XSRF_TOKEN = "plugin.xsrf.token";
    private static final String ATL_TOKEN = "atl_token";

    private final TemplateRenderer renderer;
    private final RedirectHelper redirectHelper;
    private final SignalRService signalRService;
    private final AppPropertiesService appProperties;
    private final KeysService keysService;
    private final HostPropertiesService hostProperties;
    private final PluginSettings pluginSettings;
    private final ApplicationLinkCreatorService applicationLinkCreatorService;

    @Autowired
    public ConfigPageServlet(@ComponentImport TemplateRenderer renderer,
                             RedirectHelper redirectHelper,
                             SignalRService signalRService,
                             AppPropertiesService appProperties,
                             KeysService keysService,
                             HostPropertiesService hostProperties,
                             PluginSettings pluginSettings,
                             ApplicationLinkCreatorService applicationLinkCreatorService) {
        this.renderer = renderer;
        this.redirectHelper = redirectHelper;
        this.signalRService = signalRService;
        this.appProperties = appProperties;
        this.keysService = keysService;
        this.hostProperties = hostProperties;
        this.pluginSettings = pluginSettings;
        this.applicationLinkCreatorService = applicationLinkCreatorService;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (redirectHelper.isUserLoggedInAndAdmin(request, response)) {
                response.setContentType("text/html;charset=utf-8");

                XsrfTokenGenerator xsrfTokenGenerator = ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
                String token = xsrfTokenGenerator.generateToken(request);
                Map<String, Object> parameters = new HashMap<>(buildContext());
                parameters.put(ATL_TOKEN, token);
                response.addCookie(new Cookie(PLUGIN_XSRF_TOKEN, token));

                renderer.render("templates/admin.vm", parameters, response.getWriter());
            }
        } catch (IOException | RenderingException e) {
            LOG.info(e.getMessage());
        }
    }

    @Override
    @RequiresXsrfCheck
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String pluginToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(PLUGIN_XSRF_TOKEN))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        Optional<String> atl_token = Optional.ofNullable(request.getParameter(ATL_TOKEN));

        LOG.debug("Received tokens and data in doPost. Request = {}, pluginToken = {}, atlToken = {}", request, pluginToken, atl_token.get());

        if (pluginToken == null || !atl_token.isPresent() || !pluginToken.equals(atl_token.get())) {
            return;
        }

        Optional<String> doEmbedIcons = Optional.ofNullable(request.getParameter(EMBED_ICONS));
        if (doEmbedIcons.isPresent() && !pluginSettings.getEmbedIconsSetting()) {
            pluginSettings.setEmbedIconsSetting(true);
        } else if (!doEmbedIcons.isPresent() && pluginSettings.getEmbedIconsSetting()) {
            pluginSettings.setEmbedIconsSetting(false);
        }

        Optional<String> doEmbedAvatars = Optional.ofNullable(request.getParameter(EMBED_AVATARS));
        if (doEmbedAvatars.isPresent() && !pluginSettings.getEmbedAvatarsSetting()) {
            pluginSettings.setEmbedAvatarsSetting(true);
        } else if (!doEmbedAvatars.isPresent() && pluginSettings.getEmbedAvatarsSetting()) {
            pluginSettings.setEmbedAvatarsSetting(false);
        }

        Optional<String> doEmbedProjectAvatars = Optional.ofNullable(request.getParameter(EMBED_PROJECT_AVATARS));
        if (doEmbedProjectAvatars.isPresent() && !pluginSettings.getEmbedProjectAvatarsSetting()) {
            pluginSettings.setEmbedProjectAvatarsSetting(true);
        } else if (!doEmbedProjectAvatars.isPresent() && pluginSettings.getEmbedProjectAvatarsSetting()) {
            pluginSettings.setEmbedProjectAvatarsSetting(false);
        }
    }

    private Map<String, Object> buildContext() {
        Map<String, Object> teamsContext = new HashMap<>();
        teamsContext.put("publicKey", keysService.getPublicKey());
        teamsContext.put("consumerKey", keysService.getConsumerKey());
        teamsContext.put("consumerName", PropertiesClient.MICROSOFT_TEAMS_INTEGRATION);
        teamsContext.put("atlasHome", hostProperties.getFullBaseUrl());
        teamsContext.put("atlasId", keysService.getAtlasId());
        teamsContext.put("pluginKey", appProperties.getPluginKey());
        teamsContext.put("appBaseUrl", appProperties.getTeamsAppBaseUrl());
        teamsContext.put("isConnectionActive", signalRService.isActiveConnection());
        teamsContext.put("embedIcons", pluginSettings.getEmbedIconsSetting());
        teamsContext.put("embedAvatars", pluginSettings.getEmbedAvatarsSetting());
        teamsContext.put("embedProjectAvatars", pluginSettings.getEmbedProjectAvatarsSetting());
        teamsContext.put("isApplicationLinkCreated", applicationLinkCreatorService.getApplicationLink() != null);
        teamsContext.put("applicationLinkName", applicationLinkCreatorService.getApplicationLink() != null ?
                applicationLinkCreatorService.getApplicationLink().getName() : ApplicationLinkCreatorService.TEAMS_APPLICATION_LINK_NAME);

        LOG.debug("Get keys inside the context. Consumer key = {}, Atlas id = {}, Public key = {}", keysService.getConsumerKey(), keysService.getAtlasId(), keysService.getPublicKey());

        return teamsContext;
    }

}
