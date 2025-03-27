package com.microsoft.teams.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.generic.GenericApplicationType;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;
import com.microsoft.teams.oauth.PropertiesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

@Component
public class ApplicationLinkCreatorService {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLinkCreatorService.class);
    public static final String OAUTH_INCOMING_CONSUMERKEY = "oauth.incoming.consumerkey";
    public static final String TEAMS_APPLICATION_LINK_NAME = "Microsoft Teams";
    private final MutatingApplicationLinkService mutatingApplicationLinkService;
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final ServiceProviderTokenStore serviceProviderTokenStore;
    private final TypeAccessor typeAccessor;
    private final AppPropertiesService appProperties;

    @Autowired
    public ApplicationLinkCreatorService(
            MutatingApplicationLinkService mutatingApplicationLinkService,
            ServiceProviderConsumerStore serviceProviderConsumerStore, ServiceProviderTokenStore serviceProviderTokenStore,
            TypeAccessor typeAccessor,
            AppPropertiesService appProperties) {
        this.mutatingApplicationLinkService = mutatingApplicationLinkService;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.serviceProviderTokenStore = serviceProviderTokenStore;
        this.typeAccessor = typeAccessor;
        this.appProperties = appProperties;
    }

    public void createApplicationLink(Map<String, String> properties) {
        try {
            for (ApplicationLink applicationLink : mutatingApplicationLinkService.getApplicationLinks()) {
                if (applicationLink.getRpcUrl().toString().equals(appProperties.getTeamsAppBaseUrl())) {
                    LOG.info("Application link {} already exists.", applicationLink.getRpcUrl());
                    return;
                }
            }

            LOG.debug("Creating application link.");
            ApplicationType applicationType = typeAccessor.getApplicationType(GenericApplicationType.class);
            URI appLinkUrl = new URI(appProperties.getTeamsAppBaseUrl());
            ApplicationLinkDetails applicationLinkDetails = ApplicationLinkDetails
                    .builder()
                    .name(TEAMS_APPLICATION_LINK_NAME)
                    .displayUrl(appLinkUrl)
                    .rpcUrl(appLinkUrl)
                    .isPrimary(true)
                    .build();

            ApplicationLink applicationLink = mutatingApplicationLinkService.createApplicationLink(applicationType, applicationLinkDetails);

            if (applicationLink != null) {
                String consumerKey = properties.get(PropertiesClient.CONSUMER_KEY);
                String publicKey = properties.get(PropertiesClient.PUBLIC_KEY);
                Consumer consumer = Consumer.key(consumerKey)
                        .name(PropertiesClient.MICROSOFT_TEAMS_INTEGRATION)
                        .publicKey(RSAKeys.fromPemEncodingToPublicKey(publicKey))
                        .build();

                serviceProviderConsumerStore.put(consumer);
                applicationLink.putProperty(OAUTH_INCOMING_CONSUMERKEY, consumerKey);
            }
        } catch (Exception e) {
            LOG.error("Error creating application link", e);
        }
    }

    public void removeApplicationLink() {
        try {
            String appBaseUrl = appProperties.getTeamsAppBaseUrl();
            for (ApplicationLink applicationLink : mutatingApplicationLinkService.getApplicationLinks()) {
                if (applicationLink.getRpcUrl().toString().equals(appBaseUrl)) {
                    final String consumerKey = getConsumerKey(applicationLink);
                    if (consumerKey == null) {
                        LOG.info("No consumer configured for application link {}", applicationLink.getRpcUrl());
                        return;
                    }
                    LOG.info("Removing application link {}", applicationLink.getRpcUrl());
                    serviceProviderTokenStore.removeByConsumer(consumerKey);
                    serviceProviderConsumerStore.remove(consumerKey);
                    mutatingApplicationLinkService.deleteApplicationLink(applicationLink);
                    return;
                }
            }

            LOG.info("Application link {} does not exist.", appBaseUrl);
        }
        catch (Exception e) {
            LOG.error("Error removing application link", e);
        }
    }

    public ApplicationLink getApplicationLink() {
        try {
            for (ApplicationLink applicationLink : mutatingApplicationLinkService.getApplicationLinks()) {
                if (applicationLink.getRpcUrl().toString().equals(appProperties.getTeamsAppBaseUrl())) {
                    return applicationLink;
                }
            }
        } catch (Exception e) {
            LOG.error("Error getting application link", e);
        }
        return null;
    }

    private String getConsumerKey(final ApplicationLink applicationLink) {
        final Object storedConsumerKey = applicationLink.getProperty(OAUTH_INCOMING_CONSUMERKEY);
        if (storedConsumerKey != null) {
            return storedConsumerKey.toString();
        }
        return null;
    }
}
