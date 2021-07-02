package com.microsoft.teams.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Component
public class AppPropertiesService {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (final InputStream stream =
                     AppPropertiesService.class.getResourceAsStream("/integration.properties")) {
            PROPERTIES.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getSecret() {
        return PROPERTIES.getProperty(SECRET);
    }

    public String getConsumerKey() {
        return PROPERTIES.getProperty(CONSUMER_KEY);
    }

    public String getPublicKey() {
        return PROPERTIES.getProperty(PUBLIC_KEY);
    }

    public void setPublicKey(String publicKey) {
        PROPERTIES.setProperty(PUBLIC_KEY, publicKey);
    }

    public String getPrivateKey() {
        return PROPERTIES.getProperty(PRIVATE_KEY);
    }

    public void setPrivateKey(String privateKey) {
        PROPERTIES.setProperty(PRIVATE_KEY, privateKey);
    }

    public void setTeamsAppBaseUrl(String teamsAppBaseUrl) {
        PROPERTIES.setProperty(TEAMS_APP_BASE_URL, teamsAppBaseUrl);
    }

    public String getAtlasId() {
        return PROPERTIES.getProperty(ATLAS_ID);
    }

    public String getPluginVersion() {
        return PROPERTIES.getProperty("plugin_version");
    }

    public String getPluginKey() {
        return PROPERTIES.getProperty("plugin_key");
    }

    public String getAzurePublicKeyUrl() {
        return PROPERTIES.getProperty("azure_public_key_url");
    }

    public String getIssClaim() {
        return PROPERTIES.getProperty("iss_claim");
    }

    public String getAudClaim() {
        return PROPERTIES.getProperty("aud_claim");
    }

    public String getSignalRHubUrl() {
        return PROPERTIES.getProperty("signalr_hub_url");
    }

    public String getTeamsAppBaseUrl() {
        return PROPERTIES.getProperty("teams_app_base_url");
    }
}
