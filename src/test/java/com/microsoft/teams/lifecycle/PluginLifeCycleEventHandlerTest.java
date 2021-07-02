package com.microsoft.teams.lifecycle;

import com.atlassian.sal.api.ApplicationProperties;
import com.microsoft.teams.lifecycle.scheduler.SignalRConnectionMonitorJob;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.SignalRService;
import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.service.KeysService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Testing {@link com.microsoft.teams.lifecycle.PluginLifeCycleEventHandler}
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginLifeCycleEventHandlerTest {
    private PluginLifeCycleEventHandler pluginLifeCycleEventHandler;
    @Mock
    PropertiesClient propertiesClient;
    @Mock
    ApplicationProperties applicationProperties;
    @Mock
    SignalRService signalRService;
    @Mock
    SignalRConnectionMonitorJob monitorJob;
    @Mock
    KeysService keysService;
    @Mock
    HostPropertiesService hostProperties;
    @Mock
    TeamsAtlasUserService teamsAtlasUserService;

    @Before
    public void setUp() {
        pluginLifeCycleEventHandler = new PluginLifeCycleEventHandler(propertiesClient, applicationProperties, signalRService, monitorJob, keysService, hostProperties, teamsAtlasUserService);
    }

    @Test
    public void testOnInstalled() {
        pluginLifeCycleEventHandler.onInstalled();
        verify(hostProperties, times(1)).setApplicationProperties(any(ApplicationProperties.class));
        verify(propertiesClient, times(1)).getPropertiesOrDefaults();
        verify(keysService, times(1)).updateApplicationKeys(anyMap());
        verify(propertiesClient, times(1)).saveKeysToDatabase(anyMap());
        verify(signalRService, times(1)).startSignalRConnection();
        verify(monitorJob, times(1)).registerScheduler();
        verify(monitorJob, times(1)).registerScheduler();
        verify(teamsAtlasUserService, times(1)).updateDbToAoObjects();
    }
}
