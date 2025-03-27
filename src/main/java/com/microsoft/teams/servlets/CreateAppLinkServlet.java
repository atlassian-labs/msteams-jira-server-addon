package com.microsoft.teams.servlets;

import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.ApplicationLinkCreatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CreateAppLinkServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(CreateAppLinkServlet.class);
    private final ApplicationLinkCreatorService applicationLinkCreatorService;
    private final PropertiesClient propertiesClient;
    private final RedirectHelper redirectHelper;
    @Autowired
    public CreateAppLinkServlet(ApplicationLinkCreatorService applicationLinkCreatorService, PropertiesClient propertiesClient, RedirectHelper redirectHelper) {
        this.applicationLinkCreatorService = applicationLinkCreatorService;
        this.propertiesClient = propertiesClient;
        this.redirectHelper = redirectHelper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (redirectHelper.isUserLoggedInAndAdmin(req, resp)) {
                applicationLinkCreatorService.removeApplicationLink();
                applicationLinkCreatorService.createApplicationLink(propertiesClient.getPropertiesOrDefaults());
                resp.sendRedirect(req.getContextPath() + "/plugins/servlet/teams/admin");
            }
        } catch (Exception e) {
            LOG.warn("CreateAppLinkServlet.doGet() error ({}): {}", e.getClass().getCanonicalName(), e.getMessage());
        }
    }
}
