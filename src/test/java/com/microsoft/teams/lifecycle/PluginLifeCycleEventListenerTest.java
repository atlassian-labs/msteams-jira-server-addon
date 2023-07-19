package com.microsoft.teams.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.microsoft.teams.service.AppPropertiesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Testing {@link com.microsoft.teams.lifecycle.PluginLifeCycleEventListener}
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginLifeCycleEventListenerTest {
    private PluginLifeCycleEventListener pluginLifeCycleEventListener;
    private AppPropertiesService appProperties;
    @Mock
    PluginLifeCycleEventHandler pluginLifeCycleEventHandler;
    @Mock
    PluginEventManager pluginEventManager;
    @Mock
    PluginEnabledEvent event;
    @Mock
    Plugin plugin;


    @Before
    public void setUp() {
        appProperties = new AppPropertiesService();
        pluginLifeCycleEventListener = new PluginLifeCycleEventListener(pluginLifeCycleEventHandler, pluginEventManager, appProperties);
    }

    @Test
    public void testOnPluginEnabled() {
        String pluginKey = appProperties.getPluginKey();
        when(event.getPlugin()).thenReturn(plugin);
        when(plugin.getKey()).thenReturn(pluginKey);
        pluginLifeCycleEventListener.onPluginEnabled(event);
        verify(pluginLifeCycleEventHandler, times(1)).onInstalled();
    }

    @Test
    public void testAfterPropertiesSet() {
        pluginLifeCycleEventListener.afterPropertiesSet();
        verify(pluginEventManager, times(1)).register(pluginLifeCycleEventListener);
    }
}
