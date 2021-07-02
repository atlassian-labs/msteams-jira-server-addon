package com.microsoft.teams.lifecycle;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.microsoft.teams.lifecycle.scheduler.SignalRConnectionMonitorJob;
import com.microsoft.teams.oauth.PropertiesClient;
import com.microsoft.teams.service.SignalRService;
import com.microsoft.teams.service.HostPropertiesService;
import com.microsoft.teams.service.KeysService;
import com.microsoft.teams.service.TeamsAtlasUserService;
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
    private final TeamsAtlasUserService teamsAtlasUserService;

    @Autowired
    public PluginLifeCycleEventHandler(PropertiesClient propertiesClient,
                                       ApplicationProperties applicationProperties,
                                       SignalRService signalRService,
                                       SignalRConnectionMonitorJob monitorJob,
                                       KeysService keysService,
                                       HostPropertiesService hostProperties,
                                       TeamsAtlasUserService teamsAtlasUserService) {
        this.propertiesClient = propertiesClient;
        this.applicationProperties = applicationProperties;
        this.signalRService = signalRService;
        this.monitorJob = monitorJob;
        this.keysService = keysService;
        this.hostProperties = hostProperties;
        this.teamsAtlasUserService = teamsAtlasUserService;
    }

    void onInstalled() {
        LOG.info("==========> onInstalled started <==========");

        LOG.debug("OS Name: " + System.getProperty("os.name"));
        LOG.debug("OS Arch: " + System.getProperty("os.arch"));
        LOG.debug("OS Version" + System.getProperty("os.version"));
        LOG.debug("Java version: " + System.getProperty("java.version"));

        hostProperties.setApplicationProperties(applicationProperties);

        Map<String, String> properties = propertiesClient.getPropertiesOrDefaults();

        keysService.updateApplicationKeys(properties);

        propertiesClient.saveKeysToDatabase(properties);

        signalRService.startSignalRConnection();

        monitorJob.registerScheduler();

        teamsAtlasUserService.updateDbToAoObjects();
    }

    void onUninstalled() {
    }
}
