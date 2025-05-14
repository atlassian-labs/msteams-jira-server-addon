package com.microsoft.teams.servlets;

import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.ApplicationLinkCreatorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class CreateAppLinkServletTest {

    @Mock
    private ApplicationLinkCreatorService applicationLinkCreatorService;

    @Mock
    private PropertiesClient propertiesClient;

    @Mock
    private RedirectHelper redirectHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CreateAppLinkServlet createAppLinkServlet;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoGet_UserLoggedInAndAdmin() throws Exception {
        when(redirectHelper.isUserLoggedInAndAdmin(request, response)).thenReturn(true);
        when(propertiesClient.getPropertiesOrDefaults()).thenReturn(new HashMap<>());

        createAppLinkServlet.doGet(request, response);

        verify(applicationLinkCreatorService).removeApplicationLink();
        verify(applicationLinkCreatorService).createApplicationLink(any(HashMap.class));
        verify(response).sendRedirect(request.getContextPath() + "/plugins/servlet/teams/admin");
    }

    @Test
    public void testDoGet_ExceptionThrown() throws Exception {
        when(redirectHelper.isUserLoggedInAndAdmin(request, response)).thenReturn(true);
        doThrow(new RuntimeException("Test Exception")).when(applicationLinkCreatorService).removeApplicationLink();

        createAppLinkServlet.doGet(request, response);

        verify(applicationLinkCreatorService).removeApplicationLink();
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    public void testDoGet_UserNotLoggedInOrAdmin() throws Exception {
        when(redirectHelper.isUserLoggedInAndAdmin(request, response)).thenReturn(false);

        createAppLinkServlet.doGet(request, response);

        verify(applicationLinkCreatorService, never()).removeApplicationLink();
        verify(applicationLinkCreatorService, never()).createApplicationLink(any(HashMap.class));
        verify(response, never()).sendRedirect(anyString());
    }
}
