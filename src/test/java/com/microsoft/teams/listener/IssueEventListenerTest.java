package com.microsoft.teams.listener;

import com.atlassian.event.api.EventPublisher;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.AppPropertiesService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IssueEventListenerTest {
    @Mock
    private TeamsAtlasUserService teamsAtlasUserService;
    @Mock
    private AppKeysService appKeysService;
    @Mock
    private AppPropertiesService appProperties;
    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void afterPropertiesSetThatRegisteredTest() {
        IssueEventListener issueEventListener = new IssueEventListener(eventPublisher, teamsAtlasUserService, appKeysService, appProperties);
        issueEventListener.afterPropertiesSet();

        verify(eventPublisher).register(any());
    }

    @Test
    public void verifyThatDestroyedTest() {
        IssueEventListener issueEventListener = new IssueEventListener(eventPublisher, teamsAtlasUserService, appKeysService, appProperties);
        issueEventListener.destroy();

        verify(eventPublisher).unregister(any());
    }
}