package com.microsoft.teams.service.messaging;

import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.service.RequestService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class RequestMessageHandlerTest {

    @Mock
    private TeamsMessage teamsMessage;
    @Mock
    private TeamsAtlasUserServiceImpl userService;
    @Mock
    private RequestService requestService;
    @Mock
    private ImageHelper imageHelper;
    private RequestMessageHandler requestMessageHandler;
    private TeamsMessage validTeamsMessage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        String validTeamsMessageJson = "{\"requestBody\":\"RToken\", \"atlasId\":\"RToken\",\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\"," +
                "\"request\":\"someCode\",\"token\":\"access_token\", \"requestUrl\":\"api/2/myself\",\"requestType\":\"requestType\",\"token\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\"}";
        validTeamsMessage = new TeamsMessageCreatorImpl().create(validTeamsMessageJson);
        requestMessageHandler = new RequestMessageHandler(userService, requestService, imageHelper);
    }

    @Test
    public void processMessageWithoutTeamsUser() {
        given(teamsMessage.getTeamsId()).willReturn("1");
        given(teamsMessage.getToken()).willReturn("some_token");
        given(userService.getUserByTeamsId(any())).willReturn(Collections.emptyList());

        String response = requestMessageHandler.processMessage(teamsMessage);

        assertEquals("{\"code\":401,\"response\":\"\",\"message\":\"User 1 is not authenticated\"}", response);
    }

    @Test
    public void processMessageWithUser() {
        TeamsAtlasUser atlasUser = mock(TeamsAtlasUser.class);
        when(userService.getUserByTeamsId(anyString())).thenReturn(Collections.singletonList(atlasUser));
        when(requestService.getAtlasData(any(RequestMessage.class))).thenReturn("{\"code\":200,\"message\":\"filter works\"}");

        String result = requestMessageHandler.processMessage(validTeamsMessage);
        verify(requestService).getAtlasData(any(RequestMessage.class));
        assertEquals("{\"code\":200,\"message\":\"filter works\"}", result);
    }
}