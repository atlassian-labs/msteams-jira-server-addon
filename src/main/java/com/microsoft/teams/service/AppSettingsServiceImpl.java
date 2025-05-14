package com.microsoft.teams.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.microsoft.teams.ao.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Named
@Component
public class AppSettingsServiceImpl implements AppSettingsService{
    private static final Logger LOG = LoggerFactory.getLogger(AppSettingsServiceImpl.class);

    @ComponentImport
    private final ActiveObjects activeObjects;

    @Inject
    public AppSettingsServiceImpl(ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }

    @Override
    public void set(Map<String, String> keys) {
        try {
            AppSettings[] settingsKeySet = activeObjects.find(AppSettings.class);
            AppSettings appSettings = (settingsKeySet.length == 0) ? activeObjects.create(AppSettings.class) : settingsKeySet[0];
            if (keys.containsKey(SETTINGS_EMBED_ICONS)) {
                appSettings.setEmbedIcons(Boolean.parseBoolean(keys.get(SETTINGS_EMBED_ICONS)));
            }
            if (keys.containsKey(SETTINGS_EMBED_AVATARS)) {
                appSettings.setEmbedAvatars(Boolean.parseBoolean(keys.get(SETTINGS_EMBED_AVATARS)));
            }
            if (keys.containsKey(SETTINGS_EMBED_PROJECT_AVATARS)) {
                appSettings.setEmbedProjectAvatars(Boolean.parseBoolean(keys.get(SETTINGS_EMBED_PROJECT_AVATARS)));
            }
            if (keys.containsKey(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED)) {
                appSettings.setPersonalNotifications(Boolean.parseBoolean(keys.get(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED)));
            }
            if (keys.containsKey(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED)) {
                appSettings.setGroupNotifications(Boolean.parseBoolean(keys.get(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED)));
            }
            LOG.debug("Saving keys to AO transaction started. Settings: Embed icons - {}, Embed avatars  - {}, Embed project avatars - {}, Personal notifications configured - {}, Group notifications configured - {}",
                    appSettings.getEmbedIcons(),
                    appSettings.getEmbedAvatars(),
                    appSettings.getEmbedProjectAvatars(),
                    appSettings.getPersonalNotifications(),
                    appSettings.getGroupNotifications());
            appSettings.save();
            LOG.debug("Saving app settings to AO transaction performed");
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
        }
    }

    @Override
    public Map<String, String> get() {
        Map<String, String> keys = new HashMap<>();

        try {
            AppSettings[] appSettings = activeObjects.find(AppSettings.class);
            if (appSettings.length != 0) {
                keys.put(SETTINGS_EMBED_ICONS, Boolean.toString(appSettings[0].getEmbedIcons()));
                keys.put(SETTINGS_EMBED_AVATARS, Boolean.toString(appSettings[0].getEmbedAvatars()));
                keys.put(SETTINGS_EMBED_PROJECT_AVATARS, Boolean.toString(appSettings[0].getEmbedProjectAvatars()));
                keys.put(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED, Boolean.toString(appSettings[0].getPersonalNotifications()));
                keys.put(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED, Boolean.toString(appSettings[0].getGroupNotifications()));
            }
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
        }

        return keys;
    }
}
