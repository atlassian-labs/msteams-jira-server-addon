package com.microsoft.teams.service.messaging;

import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.service.HttpClientService;
import com.microsoft.teams.service.TeamsAtlasUserServiceImpl;
import com.microsoft.teams.service.models.TeamsMessage;
import com.microsoft.teams.utils.ImageHelper;
import org.jose4j.jwt.GeneralJwtException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testing {@link com.microsoft.teams.service.messaging.CommandMessageHandler}
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandMessageHandlerTest {

    @Mock
    private TeamsAtlasUserServiceImpl userService;
    @Mock
    private ImageHelper imageHelper;
    @Mock
    private HostPropertiesService hostProperties;
    @Mock
    private HttpClientService httpClientService;
    private CommandMessageHandler spyMessageHandler;
    private TeamsMessage validTeamsMessage;
    private TeamsMessage teamsMessageWithInvalidCommand;

    @Before
    public void setUp() throws GeneralJwtException {
        String validTeamsMessageJson = "{\"command\":\"Logout\",\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\"," +
                "\"atlasId\":\"someAtlasId\",\"token\":\"sOmE_vAlId_ToKeN\"}";
        String teamsMessageJsonWithInvalidCommand = "{\"command\":\"noSuchCommand\",\"teamsId\":\"c2d0390e-4395-4802-b98b-ac3c88aa8779\"," +
                "\"atlasId\":\"someAtlasId\",\"token\":\"sOmE_vAlId_ToKeN\"}";
        validTeamsMessage = new TeamsMessageCreatorImpl().create(validTeamsMessageJson);
        teamsMessageWithInvalidCommand = new TeamsMessageCreatorImpl().create(teamsMessageJsonWithInvalidCommand);
        spyMessageHandler = Mockito.spy(new CommandMessageHandler(userService, imageHelper, hostProperties, httpClientService));
    }

    @Test
    public void processMessage_validLogoutMessage_userSuccessfullyDeleted() {
        Mockito.doNothing().when(spyMessageHandler).performUserLogout();

        String response = spyMessageHandler.processMessage(validTeamsMessage);
        Mockito.verify(userService, Mockito.times(1)).deleteAoObject("c2d0390e-4395-4802-b98b-ac3c88aa8779");
        Mockito.verify(spyMessageHandler).performUserLogout();
        assertTrue(response.contains("successfully"));
    }

    @Test
    public void processMessage_invalidCommandInMessage_userSuccessfullyDeleted() {
        String response = spyMessageHandler.processMessage(teamsMessageWithInvalidCommand);
        Mockito.verify(userService, Mockito.never()).deleteAoObject("c2d0390e-4395-4802-b98b-ac3c88aa8779");
        Mockito.verify(spyMessageHandler, Mockito.never()).performUserLogout();
        assertFalse(response.contains("successfully"));
    }
}
