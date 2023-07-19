package com.microsoft.teams.service;

import com.microsoft.teams.service.messaging.*;
import com.microsoft.teams.service.models.AuthParamMessage;
import com.microsoft.teams.service.models.RequestMessage;
import com.microsoft.teams.service.models.ResponseMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @Mock
    private AzureActiveDirectoryService azureAdService;
    @Mock
    private AuthParamMessageHandler authMessage;
    @Mock
    private SearchProjectsMessageHandler searchProjectMessage;
    @Mock
    private RequestMessageHandler requestMessage;
    @Mock
    private CommandMessageHandler commandMessage;
    @Mock
    private ImageHelper imageHelper;
    private TeamsMessageCreator teamsMessageCreator;

    private MessageService messageService;

    private String validTeamsMsg = "{\"verificationCode\":\"verification_Code\",\"requestToken\":\"request_Token\"," +
            "\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\", " + "\"token\":\"sOmE_vAlId_ToKeN\"}";
    private String teamsMsgIsInvalidJson = "this_is_not_json";
    private String validJsonWithWrongParameters = "{\"username\":\"admin\",\"password\":\"admin\",\"allowAccess\":true," +
            "\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\", \"atlasId\":\"someatlasId\"," +
            "\"token\":\"sOmE_vAlId_ToKeN\"}";

    @Before
    public void setUp() {
        teamsMessageCreator = new TeamsMessageCreatorImpl();
        messageService = new MessageService(azureAdService, searchProjectMessage, requestMessage, authMessage, commandMessage, imageHelper, teamsMessageCreator);
    }

    @Test
    public void testProcessTeamsMsgWithValidTeamsMsg() {
        when(azureAdService.isValidToken(anyString(), anyString())).thenReturn(true);

        messageService.processTeamsMsg(validTeamsMsg);

        verify(authMessage, times(1)).processMessage(any(AuthParamMessage.class));
        verify(requestMessage, times(0)).processMessage(any(RequestMessage.class));
        reset(azureAdService);
    }

    @Test
    public void testProcessTeamsMsgInvalidJson() {
        final String response = messageService.processTeamsMsg(teamsMsgIsInvalidJson);
        String expectedResult = new ResponseMessage()
            .withMessage("Teams message is invalid JSON")
            .build();

        assertEquals(expectedResult, response);
    }

    @Test
    public void testProcessTeamsMsgInvalidBearerToken() {
        when(azureAdService.isValidToken(anyString(), anyString())).thenReturn(false);
        String response = messageService.processTeamsMsg(validTeamsMsg);
        String expectedResult = new ResponseMessage()
            .withMessage(MessageService.INVALID_JWT_TOKEN)
            .build();
        
        assertEquals(expectedResult, response);
    }

    @Test
    public void testProcessTeamsMsgValidJsonWrongParameters() {
        String response = messageService.processTeamsMsg(validJsonWithWrongParameters);
        String expectedResult = new ResponseMessage()
            .withMessage("Invalid parameters in JSON message")
            .build();

        assertEquals(expectedResult, response);
    }
}
