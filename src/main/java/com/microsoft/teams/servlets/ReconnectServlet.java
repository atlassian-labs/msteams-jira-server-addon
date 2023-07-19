package com.microsoft.teams.servlets;

import com.microsoft.teams.service.SignalRService;
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
public class ReconnectServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ReconnectServlet.class);

    private final SignalRService signalRService;

    @Autowired
    public ReconnectServlet(SignalRService signalRService) {
        this.signalRService = signalRService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            signalRService.startSignalRConnection();
            resp.sendRedirect(req.getContextPath() + "/plugins/servlet/xproduct/admin");
        } catch (Exception e) {
            LOG.warn("ReconnectServlet.doGet() error ({}): {}", e.getClass().getCanonicalName(), e.getMessage());
        }
    }
}
