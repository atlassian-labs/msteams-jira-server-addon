package com.microsoft.teams.config;

import com.microsoft.teams.service.AppSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Component
public class PluginSettingsImpl implements PluginSettings {

    private final AppSettingsService appSettingsService;

    private boolean hasSettingChanged = false;

    @Autowired
    public PluginSettingsImpl(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @Override
    public boolean getEmbedIconsSetting() {
        Map<String, String> settings  = this.appSettingsService.get();
        return Boolean.parseBoolean(settings.get(SETTINGS_EMBED_ICONS));
    }

    @Override
    public void setEmbedIconsSetting(boolean doEmbedIcons) {
        if (this.getEmbedIconsSetting() != doEmbedIcons) {
            HashMap<String, String> settings = new HashMap<>();
            settings.put(SETTINGS_EMBED_ICONS, Boolean.toString(doEmbedIcons));

            this.appSettingsService.set(settings);

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getEmbedAvatarsSetting() {
        Map<String, String> settings  = this.appSettingsService.get();
        return Boolean.parseBoolean(settings.get(SETTINGS_EMBED_AVATARS));
    }

    @Override
    public void setEmbedAvatarsSetting(boolean doEmbedAvatars) {
        if (this.getEmbedAvatarsSetting() != doEmbedAvatars) {
            HashMap<String, String> settings = new HashMap<>();
            settings.put(SETTINGS_EMBED_AVATARS, Boolean.toString(doEmbedAvatars));

            this.appSettingsService.set(settings);

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getEmbedProjectAvatarsSetting() {
        Map<String, String> settings  = this.appSettingsService.get();
        return Boolean.parseBoolean(settings.get(SETTINGS_EMBED_PROJECT_AVATARS));
    }

    @Override
    public void setEmbedProjectAvatarsSetting(boolean doEmbedProjectAvatars) {
        if (this.getEmbedProjectAvatarsSetting() != doEmbedProjectAvatars) {
            HashMap<String, String> settings = new HashMap<>();
            settings.put(SETTINGS_EMBED_PROJECT_AVATARS, Boolean.toString(doEmbedProjectAvatars));

            this.appSettingsService.set(settings);

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getPersonalNotificationsSetting() {
        Map<String, String> settings  = this.appSettingsService.get();
        return Boolean.parseBoolean(settings.get(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED));
    }

    @Override
    public void setPersonalNotificationsSetting(boolean doPersonalNotifications) {
        if (this.getPersonalNotificationsSetting() != doPersonalNotifications) {
            HashMap<String, String> settings = new HashMap<>();
            settings.put(SETTINGS_PERSONAL_NOTIFICATIONS_CONFIGURED, Boolean.toString(doPersonalNotifications));

            this.appSettingsService.set(settings);

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getGroupNotificationsSetting() {
        Map<String, String> settings  = this.appSettingsService.get();
        return Boolean.parseBoolean(settings.get(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED));
    }

    @Override
    public void setGroupNotificationsSetting(boolean doGroupNotifications) {
        if (this.getGroupNotificationsSetting() != doGroupNotifications) {
            HashMap<String, String> settings = new HashMap<>();
            settings.put(SETTINGS_GROUP_NOTIFICATIONS_CONFIGURED, Boolean.toString(doGroupNotifications));

            this.appSettingsService.set(settings);

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean hasChanged() {
        return hasSettingChanged;
    }

    @Override
    public void resetObservableState() {
        hasSettingChanged = false;
    }
}