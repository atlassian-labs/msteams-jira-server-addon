package com.microsoft.teams.servlets;

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.microsoft.teams.config.PluginImageSettings;
import com.microsoft.teams.service.AppPropertiesService;
import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.service.KeysService;
import com.microsoft.teams.service.SignalRService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigPageServletTest {

    @Mock
    TemplateRenderer renderer;
    @Mock
    RedirectHelper redirectHelper;
    @Mock
    SignalRService signalRService;
    @Mock
    AppPropertiesService appProperties;
    @Mock
    KeysService keysService;
    @Mock
    HostPropertiesService hostProperties;
    @Mock
    PluginImageSettings pluginImageSettings;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    XsrfTokenGenerator xsrfTokenGenerator;
    ConfigPageServlet configPageServlet;

    private static final String EMBED_ICONS = "embed-icons";
    private static final String EMBED_AVATARS = "embed-avatars";
    private static final String EMBED_PROJECT_AVATARS = "embed-project-avatars";
    private static final String PLUGIN_XSRF_TOKEN = "plugin.xsrf.token";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.addMock(TemplateRenderer.class, renderer).addMock(XsrfTokenGenerator.class, xsrfTokenGenerator).init();
        configPageServlet = new ConfigPageServlet(renderer, redirectHelper, signalRService, appProperties, keysService, hostProperties, pluginImageSettings);
    }

    @Test
    public void doGet() {
        when(redirectHelper.isUserLoggedInAndAdmin(request, response)).thenReturn(true);

        configPageServlet.doGet(request, response);

        verify(redirectHelper).isUserLoggedInAndAdmin(request, response);
        verify(response).setContentType(anyString());
        verify(response).addCookie(any(Cookie.class));
    }

    // @Test
    // public void doPostTestSetEmBedIconsToTrue() {
    //     Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

    //     when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
    //     when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

    //     given(request.getParameter(EMBED_ICONS)).willReturn("embed_icons");
    //     given(pluginImageSettings.getEmbedIconsSetting()).willReturn(false);
    //     given(request.getCookies()).willReturn(new Cookie[]{ cookie});

    //     configPageServlet.doPost(request, response);

    //     verify(pluginImageSettings).setEmbedIconsSetting(true);
    // }

    // @Test
    // public void doPostTestSetEmBedIconsToFalse() {
    //     Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

    //     when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
    //     when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

    //     given(request.getParameter(EMBED_ICONS)).willReturn(null);
    //     given(pluginImageSettings.getEmbedIconsSetting()).willReturn(true);
    //     given(request.getCookies()).willReturn(new Cookie[]{ cookie});

    //     configPageServlet.doPost(request, response);

    //     verify(pluginImageSettings).setEmbedIconsSetting(false);
    // }

//   @Test
//     public void doPostTestSetEmBedAvatarsToTrue() {
//         Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

//         when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
//         when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

//         given(request.getParameter(EMBED_AVATARS)).willReturn("embed_avatars");
//         given(pluginImageSettings.getEmbedAvatarsSetting()).willReturn(false);
//         given(request.getCookies()).willReturn(new Cookie[]{ cookie});

//         configPageServlet.doPost(request, response);

//         verify(pluginImageSettings).setEmbedAvatarsSetting(true);
//     }

//     @Test
//     public void doPostTestSetEmBedAvatarsToFalse() {
//         Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

//         when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
//         when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

//         given(request.getParameter(EMBED_AVATARS)).willReturn(null);
//         given(pluginImageSettings.getEmbedAvatarsSetting()).willReturn(true);
//         given(request.getCookies()).willReturn(new Cookie[]{ cookie});

//         configPageServlet.doPost(request, response);

//         verify(pluginImageSettings).setEmbedAvatarsSetting(false);
//     }

//     @Test
//     public void doPostTestSetEmBedProjectAvatarsToTrue() {
//         Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

//         when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
//         when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

//         given(request.getParameter(EMBED_PROJECT_AVATARS)).willReturn("embed_project_avatars");
//         given(pluginImageSettings.getEmbedProjectAvatarsSetting()).willReturn(false);
//         given(request.getCookies()).willReturn(new Cookie[]{ cookie});

//         configPageServlet.doPost(request, response);

//         verify(pluginImageSettings).setEmbedProjectAvatarsSetting(true);
//     }

//     @Test
//     public void doPostTestSetEmBedProjectAvatarsToFalse() {
//         Cookie cookie = new Cookie("plugin.xsrf.token", "plugin.xsrf.token");

//         when(xsrfTokenGenerator.getToken(any(HttpServletRequest.class))).thenReturn(cookie.getName());
//         when(xsrfTokenGenerator.validateToken(request, cookie.getName())).thenReturn(true);

//         given(request.getParameter(EMBED_PROJECT_AVATARS)).willReturn(null);
//         given(pluginImageSettings.getEmbedProjectAvatarsSetting()).willReturn(true);
//         given(request.getCookies()).willReturn(new Cookie[]{ cookie});

//         configPageServlet.doPost(request, response);

//         verify(pluginImageSettings).setEmbedProjectAvatarsSetting(false);
//     }
}