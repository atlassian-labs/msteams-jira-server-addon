package com.microsoft.teams.config;

import com.microsoft.teams.service.AppSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.teams.oauth.PropertiesClient.*;

@Component
public class PluginImageSettingsImpl implements PluginImageSettings{

    private final AppSettingsService appSettingsService;

    private boolean hasSettingChanged = false;

    @Autowired
    public PluginImageSettingsImpl(AppSettingsService appSettingsService) {
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
    public boolean hasChanged() {
        return hasSettingChanged;
    }

    @Override
    public void resetObservableState() {
        hasSettingChanged = false;
    }
}