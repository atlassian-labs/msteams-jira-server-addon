package com.microsoft.teams.servlets;

import com.microsoft.teams.service.KeysService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JiraServerIdServletTest {

    @Mock
    private KeysService keysService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    private JiraServerIdServlet jiraServerIdServlet;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jiraServerIdServlet = new JiraServerIdServlet(keysService);
    }

    @Test
    public void testDoGetSuccess() throws Exception {
        String atlasId = "test-atlas-id";
        when(keysService.getAtlasId()).thenReturn(atlasId);
        when(response.getWriter()).thenReturn(writer);

        jiraServerIdServlet.doGet(request, response);

        verify(response).getWriter();
        verify(writer).println(atlasId);
        verify(writer).close();
    }
}