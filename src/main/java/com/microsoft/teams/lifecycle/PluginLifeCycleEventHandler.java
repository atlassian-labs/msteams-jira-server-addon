package com.microsoft.teams.lifecycle;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.microsoft.teams.lifecycle.scheduler.SignalRConnectionMonitorJob;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PluginLifeCycleEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLifeCycleEventHandler.class);

    private final PropertiesClient propertiesClient;
    private final SignalRService signalRService;
    @ComponentImport
    private final ApplicationProperties applicationProperties;
    private final SignalRConnectionMonitorJob monitorJob;
    private final KeysService keysService;
    private final HostPropertiesService hostProperties;
    private final AoService aoService;
    private final ApplicationLinkCreatorService applicationLinkCreatorService;

    @Autowired
    public PluginLifeCycleEventHandler(PropertiesClient propertiesClient,
                                       ApplicationProperties applicationProperties,
                                       SignalRService signalRService,
                                       SignalRConnectionMonitorJob monitorJob,
                                       KeysService keysService,
                                       HostPropertiesService hostProperties,
                                       AoService aoService,
                                       ApplicationLinkCreatorService applicationLinkCreatorService) {
        this.propertiesClient = propertiesClient;
        this.applicationProperties = applicationProperties;
        this.signalRService = signalRService;
        this.monitorJob = monitorJob;
        this.keysService = keysService;
        this.hostProperties = hostProperties;
        this.aoService = aoService;
        this.applicationLinkCreatorService = applicationLinkCreatorService;
    }

    void onInstalled() {
        LOG.info("==========> onInstalled started <==========");

        LOG.debug("OS Name: " + System.getProperty("os.name"));
        LOG.debug("OS Arch: " + System.getProperty("os.arch"));
        LOG.debug("OS Version" + System.getProperty("os.version"));
        LOG.debug("Java version: " + System.getProperty("java.version"));

        hostProperties.setApplicationProperties(applicationProperties);

        Map<String, String> properties = propertiesClient.getPropertiesOrDefaults();
        Map<String, String> settings = propertiesClient.getSettingsOrDefaults();

        keysService.updateApplicationKeys(properties);

        propertiesClient.saveKeysToDatabase(properties);

        propertiesClient.saveSettingsToDatabase(settings);

        signalRService.startSignalRConnection();

        monitorJob.registerScheduler();

        aoService.updateDbToAoObjects();

        applicationLinkCreatorService.createApplicationLink(properties);
    }

    void onUninstalled() {
        applicationLinkCreatorService.removeApplicationLink();
    }
}
