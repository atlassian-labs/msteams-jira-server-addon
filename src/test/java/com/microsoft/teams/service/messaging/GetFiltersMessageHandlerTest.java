package com.microsoft.teams.service.messaging;

import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.jose4j.jwt.GeneralJwtException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class GetFiltersMessageHandlerTest {
    @Mock
    private TeamsAtlasUserServiceImpl userService;
    @Mock
    private RequestService requestService;
    @Mock
    private ImageHelper imageHelper;
    @Mock
    private JiraThreadLocalUtil jiraThreadLocalUtil;
    private GetFiltersMessageHandler messageHandler;
    private TeamsMessage validTeamsMessage;

    @Before
    public void setUp() throws GeneralJwtException {
        MockitoAnnotations.initMocks(this);
        String validTeamsMessageJson = "{\"teamsId\":\"123\",\"atlasId\":\"someAtlasId\",\"requestUrl\":\"api/2/filter\",\"requestType\":\"GET\",\"requestBody\":\"{\\\"filterName\\\":null}\",\"token\":\"someToken\"}";
        validTeamsMessage = new TeamsMessageCreatorImpl().create(validTeamsMessageJson);
        messageHandler = new GetFiltersMessageHandler(userService, requestService, imageHelper, jiraThreadLocalUtil);
    }

    @Test
    public void processMessageWithoutUser() {
        String result = messageHandler.processMessage(validTeamsMessage);
        assertEquals("{\"code\":401,\"response\":\"\",\"message\":\"User 123 is not authenticated\"}", result);
    }
}