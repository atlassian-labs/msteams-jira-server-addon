package com.microsoft.teams.servlets;

import com.atlassian.annotations.security.AnonymousSiteAccess;
import com.microsoft.teams.service.KeysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
@AnonymousSiteAccess
public class JiraServerIdServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(JiraServerIdServlet.class);

    private final KeysService keysService;

    @Autowired
    public JiraServerIdServlet(KeysService keysService) {
        this.keysService = keysService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String atlasId = keysService.getAtlasId();

            final PrintWriter w = resp.getWriter();
            w.println(atlasId);
            w.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
