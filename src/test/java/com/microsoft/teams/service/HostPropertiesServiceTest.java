package com.microsoft.teams.service;

import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class HostPropertiesServiceTest {

    HostPropertiesService hostPropertiesService;
    @Mock
    ApplicationProperties applicationProperties;
    final String baseUrl = "http://atlassian/some_base_url";
    final String displayName = "displayName";
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getBaseUrl() {
        hostPropertiesService = new HostPropertiesService();
        when(applicationProperties.getBaseUrl()).thenReturn(baseUrl);
        when(applicationProperties.getDisplayName()).thenReturn(displayName);
        hostPropertiesService.setApplicationProperties(applicationProperties);

        String hostBaseUrl = hostPropertiesService.getBaseUrl();

        assertEquals("http://atlassian", hostBaseUrl);
        assertEquals("/some_base_url", hostPropertiesService.getContextPath());
        assertEquals(displayName, hostPropertiesService.getDisplayName());
    }

    @Test
    public void getFullBaseUrl() {
        hostPropertiesService = new HostPropertiesService();
        when(applicationProperties.getBaseUrl()).thenReturn("http://atlassian");
        when(applicationProperties.getDisplayName()).thenReturn(displayName);
        hostPropertiesService.setApplicationProperties(applicationProperties);

        String hostBaseUrl = hostPropertiesService.getBaseUrl();

        assertEquals(hostPropertiesService.getFullBaseUrl(), hostBaseUrl);
        assertEquals("", hostPropertiesService.getContextPath());
        assertEquals(displayName, hostPropertiesService.getDisplayName());
    }
}