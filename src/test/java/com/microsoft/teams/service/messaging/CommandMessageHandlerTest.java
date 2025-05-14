package com.microsoft.teams.service.messaging;

import com.microsoft.teams.config.PluginSettings;
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
    @Mock
    private PluginSettings pluginSettings;
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
        spyMessageHandler = Mockito.spy(new CommandMessageHandler(userService, imageHelper, hostProperties, httpClientService, pluginSettings));
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

    @Test
    public void processMessage_enablePersonalNotifications_personalNotificationsEnabled() throws GeneralJwtException {
        String enablePersonalNotificationsMessageJson = "{\"command\":\"EnablePersonalNotifications\"}";
        TeamsMessage enablePersonalNotificationsMessage = new TeamsMessageCreatorImpl().create(enablePersonalNotificationsMessageJson);

        String response = spyMessageHandler.processMessage(enablePersonalNotificationsMessage);

        Mockito.verify(pluginSettings, Mockito.times(1)).setPersonalNotificationsSetting(true);
        assertTrue(response.contains("successfully enabled"));
    }

    @Test
    public void processMessage_disablePersonalNotifications_personalNotificationsDisabled() throws GeneralJwtException {
        String disablePersonalNotificationsMessageJson = "{\"command\":\"DisablePersonalNotifications\"}";
        TeamsMessage disablePersonalNotificationsMessage = new TeamsMessageCreatorImpl().create(disablePersonalNotificationsMessageJson);

        String response = spyMessageHandler.processMessage(disablePersonalNotificationsMessage);

        Mockito.verify(pluginSettings, Mockito.times(1)).setPersonalNotificationsSetting(false);
        assertTrue(response.contains("successfully disabled"));
    }

    @Test
    public void processMessage_enableChannelNotifications_channelNotificationsEnabled() throws GeneralJwtException {
        String enableChannelNotificationsMessageJson = "{\"command\":\"EnableChannelNotifications\"}";
        TeamsMessage enableChannelNotificationsMessage = new TeamsMessageCreatorImpl().create(enableChannelNotificationsMessageJson);

        String response = spyMessageHandler.processMessage(enableChannelNotificationsMessage);

        Mockito.verify(pluginSettings, Mockito.times(1)).setGroupNotificationsSetting(true);
        assertTrue(response.contains("successfully enabled"));
    }

    @Test
    public void processMessage_disableChannelNotifications_channelNotificationsDisabled() throws GeneralJwtException {
        String disableChannelNotificationsMessageJson = "{\"command\":\"DisableChannelNotifications\"}";
        TeamsMessage disableChannelNotificationsMessage = new TeamsMessageCreatorImpl().create(disableChannelNotificationsMessageJson);

        String response = spyMessageHandler.processMessage(disableChannelNotificationsMessage);

        Mockito.verify(pluginSettings, Mockito.times(1)).setGroupNotificationsSetting(false);
        assertTrue(response.contains("successfully disabled"));
    }
}
