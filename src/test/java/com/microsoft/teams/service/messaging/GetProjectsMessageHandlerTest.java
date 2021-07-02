package com.microsoft.teams.service.messaging;

import com.microsoft.teams.service.HostPropertiesService;
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

public class GetProjectsMessageHandlerTest {
    @Mock
    private TeamsAtlasUserServiceImpl userService;
    @Mock
    private RequestService requestService;
    @Mock
    private ImageHelper imageHelper;
    @Mock
    private HostPropertiesService hostPropertiesService;
    private GetProjectsMessageHandler messageHandler;
    private TeamsMessage validTeamsMessage;

    @Before
    public void setUp() throws GeneralJwtException {
        MockitoAnnotations.initMocks(this);
        String validTeamsMessageJson = "{\"teamsId\":\"123\",\"atlasId\":\"someAtlasId\",\"requestUrl\":\"api/2/myself\",\"requestType\":\"GET\",\"requestBody\":\"\",\"token\":\"someToken\"}";
        validTeamsMessage = new TeamsMessageCreatorImpl().create(validTeamsMessageJson);
        messageHandler = new GetProjectsMessageHandler(userService, requestService, hostPropertiesService, imageHelper);
    }

    @Test
    public void processMessageWithoutUser() {
        String result = messageHandler.processMessage(validTeamsMessage);
        assertEquals("{\"code\":401,\"response\":\"\",\"message\":\"User 123 is not authenticated\"}", result);
    }
}