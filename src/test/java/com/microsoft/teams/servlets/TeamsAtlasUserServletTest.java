package com.microsoft.teams.servlets;

import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class TeamsAtlasUserServletTest {

    private TeamsAtlasUserServlet servlet;
    @Mock
    private TeamsAtlasUserService userService;
    @Mock
    private AppKeysService keysService;
    @Mock
    private RedirectHelper redirectHelper;
    @Mock
    private HttpServletRequest req;
    @Mock
    private HttpServletResponse resp;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        servlet = new TeamsAtlasUserServlet(userService, keysService, redirectHelper);
    }

    @Test
    public void doPostTestOnDeleteAll() throws IOException {
        given(redirectHelper.isUserLoggedInAndAdmin(req, resp)).willReturn(true);
        when(req.getParameter("method")).thenReturn("deleteAll");

        servlet.doPost(req, resp);
        verify(userService).deleteAll();
        verify(resp).sendRedirect(anyString());
    }

    @Test
    public void doPostTestOnDelete() throws IOException {
        given(redirectHelper.isUserLoggedInAndAdmin(req, resp)).willReturn(true);
        when(req.getParameter("method")).thenReturn("delete");
        when(req.getParameter("MsTeamsUserId")).thenReturn("123");

        servlet.doPost(req, resp);
        verify(userService).deleteAoObject("123");
        verify(resp).sendRedirect(anyString());
    }

    @Test
    public void doGet() throws IOException {
        final Integer WRITE_METHOD_CALLED_TIMES = 36;
        final Integer PRINTF_METHOD_CALLED_TIMES = 4;
        StringWriter sw = new StringWriter();
        PrintWriter pw = spy(new PrintWriter(sw));

        given(redirectHelper.isUserLoggedInAndAdmin(req, resp)).willReturn(true);
        given(resp.getWriter()).willReturn(pw);
        given(userService.all()).willReturn(Collections.emptyList());

        servlet.doGet(req, resp);

        verify(pw, times(WRITE_METHOD_CALLED_TIMES)).write(anyString());
        verify(pw, times(PRINTF_METHOD_CALLED_TIMES)).printf(anyString(), any());
    }
}