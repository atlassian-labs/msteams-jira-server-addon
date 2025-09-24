package com.microsoft.teams.servlets;

import com.microsoft.teams.service.SignalRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ReconnectServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ReconnectServlet.class);
    private final SignalRService signalRService;
    private final RedirectHelper redirectHelper;

    @Autowired
    public ReconnectServlet(SignalRService signalRService, RedirectHelper redirectHelper) {
        this.signalRService = signalRService;
        this.redirectHelper = redirectHelper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (redirectHelper.isUserLoggedInAndAdmin(req, resp)) {
                signalRService.startSignalRConnection();
                resp.sendRedirect(req.getContextPath() + "/plugins/servlet/teams/admin");
            }
        } catch (Exception e) {
            LOG.warn("ReconnectServlet.doGet() error ({}): {}", e.getClass().getCanonicalName(), e.getMessage());
        }
    }
}
