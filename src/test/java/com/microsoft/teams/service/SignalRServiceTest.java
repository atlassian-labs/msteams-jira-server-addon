package com.microsoft.teams.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SignalRServiceTest {

    @Mock
    private MessageService messageService;
    @Mock
    private AppPropertiesService appProperties;
    @Mock
    private KeysService keysService;
    @Mock
    private HostPropertiesService hostProperties;
    private SignalRService spySignalRService;

    @Before
    public void setUp() {
        spySignalRService = Mockito.spy(new SignalRService(messageService, appProperties, keysService, hostProperties));
    }

    @Test
    public void testStartSignalRConnection() {
        assertFalse(spySignalRService.isActiveConnection());

        Mockito.when(appProperties.getPluginVersion()).thenReturn("2019.7.22");
        Mockito.when(appProperties.getSignalRHubUrl()).thenReturn("https://jira-server-staging.msteams-atlassian.com/JiraGateway?atlasId=%s&atlasUrl=%s&pluginVersion=%s");
        Mockito.when(keysService.getAtlasId()).thenReturn("1111-2222-3333-4444");
        Mockito.when(hostProperties.getFullBaseUrl()).thenReturn("http://localhost:2990/jira");

        spySignalRService.startSignalRConnection();
        Mockito.verify(spySignalRService).startSignalRConnection();

        assertTrue(spySignalRService.isActiveConnection());

        spySignalRService.destroy();
    }

}