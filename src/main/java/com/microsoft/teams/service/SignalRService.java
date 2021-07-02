package com.microsoft.teams.service;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
public class SignalRService implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SignalRService.class);
    private HubConnection hubConnection;
    private final MessageService messageService;
    private static final String SIGNALR_HUB_METHOD_NAME = "MakeRequest";
    private boolean isActiveConnection;
    private final AppPropertiesService appProperties;
    private final KeysService keysService;
    private final HostPropertiesService hostProperties;
    private volatile boolean disposed = false;
    private volatile boolean disposing = false;

    @Autowired
    public SignalRService(MessageService messageService,
                          AppPropertiesService appProperties,
                          KeysService keysService,
                          HostPropertiesService hostProperties) {
        this.messageService = messageService;
        this.appProperties = appProperties;
        this.keysService = keysService;
        this.hostProperties = hostProperties;
    }

    public void startSignalRConnection() {
        logMemoryUsage("[startSignalRConnection()] Before start connection");
        initConnection();
        startConnectionIfDisconnected();
        logMemoryUsage("[startSignalRConnection()] After start connection");
    }

    private void updateConnectionStatus() {
        boolean connectionState = hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
        this.isActiveConnection = connectionState;
        LOG.info("SignalR connection is currently {}", connectionState);
    }

    private void initConnection() {
        if (hubConnection == null) {
            String pluginVersion = appProperties.getPluginVersion();
            String connectionHubUrl = String.format(appProperties.getSignalRHubUrl(), keysService.getAtlasId(), hostProperties.getFullBaseUrl(), pluginVersion);
            LOG.info("Creating new SignalR connection with HUB url: {}", connectionHubUrl);
            hubConnection = HubConnectionBuilder.create(connectionHubUrl).build();
            hubConnection.on(SIGNALR_HUB_METHOD_NAME, this::processRequest, String.class, String.class);
            // KeepAliveInterval = 15 secs on server, recommended value x2
            hubConnection.setServerTimeout(30 * 1000);
            hubConnection.onClosed(ex -> {
                LOG.info("Hub connection closed");
                this.isActiveConnection = false;
                if (ex != null) {
                    exceptionLogExtender("initConnection ", Level.DEBUG, ex);
                    logMemoryUsage("[initConnection()] On Error");
                }
                if (!disposing) {
                    startConnectionIfDisconnected();
                }
            });
        }
    }

    private void logMemoryUsage(String message) {
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
        LOG.info(String.format("[%s]. Used memory: %s MB", message, usedMemory));
    }

    private void startConnectionIfDisconnected() {
        if (disposed || disposing) {
            return;
        }
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
            LOG.info("Starting new SignalR connection as its status was DISCONNECTED");
            try {
                hubConnection.start().blockingAwait();
                LOG.info("SignalR connection has been reconnected");
            } catch (Exception e) {
                String signature = "startConnectionIfDisconnected() error ";
                exceptionLogExtender(signature, Level.DEBUG, e);
            }
        }
        // always set status
        updateConnectionStatus();
    }

    private void processRequest(String identifier, String teamsMsg) {
        LOG.info("Message received: {}", teamsMsg);
        String response = null;
        try {
            response = messageService.processTeamsMsg(teamsMsg);
        } catch (Exception e) {
            String signature = "processRequest() error ";
            exceptionLogExtender(signature, Level.DEBUG, e);
        }
        sendResponse(identifier, response);
    }

    private void sendResponse(String identifier, String response) {
        LOG.debug("SignalR invoke hub method Callback() with response: {}", cutEncodedImage(response));
        try {
            hubConnection.send("Callback", identifier, response);
        } catch (Exception e) {
            String signature = "sendResponse() error ";
            exceptionLogExtender(signature, Level.DEBUG, e);
        }
    }

    public boolean isActiveConnection() {
        return isActiveConnection;
    }

    @Override
    public void destroy() {
        disposing = true;
        dispose();
    }

    protected void dispose()
    {
        if (disposed) {
            return;
        }

        if (disposing) {
            hubConnection.stop();
            updateConnectionStatus();
            LOG.debug("SignalRService destroyed");
        }

        disposed = true;
    }

    private String cutEncodedImage(String response) {
        Pattern logPattern = Pattern.compile("base64,(.*?)\"");
        Matcher matcher = logPattern.matcher(response);
        String responseWithoutImage = response;
        while (matcher.find()) {
            responseWithoutImage = responseWithoutImage.replace(matcher.group(1),"(DELETED)");
        }
        return responseWithoutImage;
    }
}
