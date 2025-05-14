package com.microsoft.teams.servlets;

import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;

//TODO remove this servlet on production

public class TeamsAtlasUserServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TeamsAtlasUserServlet.class);
    private final TeamsAtlasUserService userService;
    private final AppKeysService keysService;
    private final RedirectHelper redirectHelper;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public TeamsAtlasUserServlet(TeamsAtlasUserService userService,
                                 AppKeysService keysService,
                                 RedirectHelper redirectHelper) {
        this.userService = userService;
        this.keysService = keysService;
        this.redirectHelper = redirectHelper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (redirectHelper.isUserLoggedInAndAdmin(req, resp)) {
            try {
                final PrintWriter w = resp.getWriter();
                w.write("<h1>Teams-Atlas Mapping</h1>");
                w.write("<form method=\"post\">");
                w.write("MsTeamsUserID:<br>");
                w.write("<input type=\"text\" name=\"MsTeamsUserId\" size=\"25\"/><br>");
                w.write("AtlasAccessToken:<br>");
                w.write("<input type=\"text\" name=\"AtlasAccessToken\" size=\"25\"/><br>");
                w.write("<input type=\"hidden\" name=\"method\" value=\"post\"/>");
                w.write("  ");
                w.write("<input type=\"submit\" name=\"submit\" value=\"Add\"/>");
                w.write("</form>");

                w.write("<table>");
                w.write("<tr>");
                w.write("<th>MsTeamsUserId</th>");
                w.write("<th>AtlasAccessToken</th>");
                w.write("<th>Created</th>");
                w.write("<th>Modified</th>");
                w.write("<th>Delete User</th>");
                w.write("</tr>");
                for (TeamsAtlasUser user : userService.all()) {
                    w.write("<tr>");
                    w.printf("<td> %s </td>", user.getMsTeamsUserId());
                    w.printf("<td> %s </td>", user.getAtlasAccessToken());
                    w.printf("<td> %s </td>", user.getDateCreated() != null ? dateFormat.format(user.getDateCreated()) : "");
                    w.printf("<td> %s </td>", user.getDateUpdated() != null ? dateFormat.format(user.getDateUpdated()) : "");
                    w.write("<td>");
                    w.write("<form method=\"post\">");
                    w.write("<input type=\"hidden\" name=\"method\" value=\"delete\"/>");
                    w.printf("<input type=\"hidden\" name=\"MsTeamsUserId\" value=\"%s\"/>", user.getMsTeamsUserId());
                    w.write("<input type=\"submit\" name=\"delete\" value=\"Delete\"/>");
                    w.write("</form>");
                    w.write("</td>");
                    w.write("</tr>");
                }
                w.write("</table>");

                w.write("<form method=\"post\">");
                w.write("<input type=\"hidden\" name=\"method\" value=\"deleteAll\"/>");
                w.write("<input type=\"submit\" name=\"deleteAll\" value=\"Delete All\"/>");
                w.write("</form>");

                Map<String, String> keys = keysService.get();
                w.printf("<b>Consumer Key:</b> %s<br>", keys.get(CONSUMER_KEY));
                w.printf("<b>Public Key:</b> %s<br>", keys.get(PUBLIC_KEY));
                w.printf("<b>Private Key:</b> %s<br>", keys.get(PRIVATE_KEY));
                w.printf("<b>Atlas Id:</b> %s<br>", keys.get(ATLAS_ID));

                w.write("<script language='javascript'>document.forms[0].elements[0].focus();</script>");

                w.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (redirectHelper.isUserLoggedInAndAdmin(req, resp)) {
            try {
                String servletPath = req.getContextPath() + "/plugins/servlet/teams/user/mapping";
                Map<String, String> properties = new HashMap<>();
                if ("deleteAll".equals(req.getParameter("method"))) {
                    userService.deleteAll();
                    resp.sendRedirect(servletPath);
                } else if ("delete".equals(req.getParameter("method"))) {
                    userService.deleteAoObject(req.getParameter("MsTeamsUserId"));
                    resp.sendRedirect(servletPath);
                } else {
                    properties.put(TEAMS_ID, req.getParameter("MsTeamsUserId"));
                    properties.put(ACCESS_TOKEN, req.getParameter("AtlasAccessToken"));
                    userService.add(properties);

                    resp.sendRedirect(servletPath);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
