package com.microsoft.teams.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.application.generic.GenericApplicationType;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.Consumer;
import com.microsoft.teams.oauth.PropertiesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationLinkCreatorServiceTest {

    @Mock
    private MutatingApplicationLinkService mutatingApplicationLinkService;

    @Mock
    private ServiceProviderConsumerStore serviceProviderConsumerStore;

    @Mock
    private ServiceProviderTokenStore serviceProviderTokenStore;

    @Mock
    private TypeAccessor typeAccessor;

    @Mock
    private AppPropertiesService appProperties;

    @Mock
    private ApplicationLink applicationLink;

    @InjectMocks
    private ApplicationLinkCreatorService applicationLinkCreatorService;


    @Test
    void testCreateApplicationLink() throws Exception {
        String baseUrl = "http://example.com";
        when(appProperties.getTeamsAppBaseUrl()).thenReturn(baseUrl);
        when(mutatingApplicationLinkService.getApplicationLinks()).thenReturn(Collections.emptyList());
        when(mutatingApplicationLinkService.createApplicationLink(any(GenericApplicationType.class), any(ApplicationLinkDetails.class))).thenReturn(applicationLink);
        when(typeAccessor.getApplicationType(GenericApplicationType.class)).thenReturn(mock(GenericApplicationType.class));

        Map<String, String> properties = new HashMap<>();
        properties.put(PropertiesClient.CONSUMER_KEY, "consumerKey");
        properties.put(PropertiesClient.PUBLIC_KEY, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgmnIS5p0nlsa4QUpRDOaacM+KZsLodlGE63R8YrrF8rsyMT0OEq0x8Bkt6zUwVOP6V14g03syqRBn61GuD13AiwESGKKqFZ0CpZqVLVHJm0fA955itC6keiPY4jB58QhcEzmmI+yo8GLeaXEumMpIU6N6uPSJUnDKd4vS58sQzjItsHI+D95zkyIKS0lngjn/esvYp+4FvtL+tllwmbf6Ca7L5qDyU6Dy3stZyiBSkYgF5AMtpw0haKJchegtO4lFRAeNmw3ndDWQOLxak45gf76gr5ejQY3u6m8CfBQX14g9Wueu+1CY1ck3csFfkelqaqohD0gOBh9241bJxh2dQIDAQAB");

        applicationLinkCreatorService.createApplicationLink(properties);

        verify(mutatingApplicationLinkService, times(1)).createApplicationLink(any(GenericApplicationType.class), any(ApplicationLinkDetails.class));
        verify(serviceProviderConsumerStore, times(1)).put(any(Consumer.class));
    }

    @Test
    void testRemoveApplicationLink() throws Exception {
        String baseUrl = "http://example.com";
        when(appProperties.getTeamsAppBaseUrl()).thenReturn(baseUrl);

        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getRpcUrl()).thenReturn(new URI(baseUrl));
        when(applicationLink.getProperty(ApplicationLinkCreatorService.OAUTH_INCOMING_CONSUMERKEY)).thenReturn("consumerKey");
        when(mutatingApplicationLinkService.getApplicationLinks()).thenReturn(Collections.singletonList(applicationLink));

        applicationLinkCreatorService.removeApplicationLink();

        verify(serviceProviderTokenStore, times(1)).removeByConsumer("consumerKey");
        verify(serviceProviderConsumerStore, times(1)).remove("consumerKey");
        verify(mutatingApplicationLinkService, times(1)).deleteApplicationLink(applicationLink);
    }

    @Test
    void testCreateApplicationLinkAlreadyExists() throws Exception {
        String baseUrl = "http://example.com";
        when(appProperties.getTeamsAppBaseUrl()).thenReturn(baseUrl);
        when(mutatingApplicationLinkService.getApplicationLinks()).thenReturn(Collections.singletonList(applicationLink));
        when(applicationLink.getRpcUrl()).thenReturn(new URI(baseUrl));

        applicationLinkCreatorService.createApplicationLink(new HashMap<>());

        verify(mutatingApplicationLinkService, never()).createApplicationLink(any(GenericApplicationType.class), any(ApplicationLinkDetails.class));
        verify(serviceProviderConsumerStore, never()).put(any(Consumer.class));
    }

    @Test
    void testCreateApplicationLinkException() throws Exception {
        when(appProperties.getTeamsAppBaseUrl()).thenThrow(new RuntimeException("Test Exception"));

        applicationLinkCreatorService.createApplicationLink(new HashMap<>());

        verify(mutatingApplicationLinkService, never()).createApplicationLink(any(GenericApplicationType.class), any(ApplicationLinkDetails.class));
        verify(serviceProviderConsumerStore, never()).put(any(Consumer.class));
    }

    @Test
    void testRemoveApplicationLinkNotExists() throws Exception {
        String baseUrl = "http://example.com";
        when(appProperties.getTeamsAppBaseUrl()).thenReturn(baseUrl);
        when(mutatingApplicationLinkService.getApplicationLinks()).thenReturn(Collections.emptyList());

        applicationLinkCreatorService.removeApplicationLink();

        verify(serviceProviderTokenStore, never()).removeByConsumer(anyString());
        verify(serviceProviderConsumerStore, never()).remove(anyString());
        verify(mutatingApplicationLinkService, never()).deleteApplicationLink(any(ApplicationLink.class));
    }

    @Test
    void testRemoveApplicationLinkNoConsumerKey() throws Exception {
        String baseUrl = "http://example.com";
        when(appProperties.getTeamsAppBaseUrl()).thenReturn(baseUrl);

        ApplicationLink applicationLink = mock(ApplicationLink.class);
        when(applicationLink.getRpcUrl()).thenReturn(new URI(baseUrl));
        when(applicationLink.getProperty(ApplicationLinkCreatorService.OAUTH_INCOMING_CONSUMERKEY)).thenReturn(null);
        when(mutatingApplicationLinkService.getApplicationLinks()).thenReturn(Collections.singletonList(applicationLink));

        applicationLinkCreatorService.removeApplicationLink();

        verify(serviceProviderTokenStore, never()).removeByConsumer(anyString());
        verify(serviceProviderConsumerStore, never()).remove(anyString());
        verify(mutatingApplicationLinkService, never()).deleteApplicationLink(applicationLink);
    }
}
