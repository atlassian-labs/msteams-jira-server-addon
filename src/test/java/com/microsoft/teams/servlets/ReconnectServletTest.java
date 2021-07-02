package com.microsoft.teams.servlets;

import com.microsoft.teams.service.SignalRService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ReconnectServletTest {

    private ReconnectServlet servlet;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private SignalRService signalRService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        servlet = new ReconnectServlet(signalRService);
    }

    @Test
    public void doGetTest() throws ServletException, IOException {
        servlet.doGet(httpServletRequest, httpServletResponse);
        verify(signalRService, Mockito.times(1)).startSignalRConnection();
        verify(httpServletResponse, Mockito.times(1)).sendRedirect(anyString());
    }
}