package com.microsoft.teams.config;

import org.springframework.stereotype.Component;

@Component
public class PluginImageSettingsImpl implements PluginImageSettings{

    // Product decision - embed icons by default
    private boolean doEmbedIcons = true;
    private boolean doEmbedAvatars;
    private boolean doEmbedProjectAvatars = true;

    private boolean hasSettingChanged = false;

    @Override
    public boolean getEmbedIconsSetting() {
        return doEmbedIcons;
    }

    @Override
    public void setEmbedIconsSetting(boolean doEmbedIcons) {
        if (this.doEmbedIcons != doEmbedIcons) {
            this.doEmbedIcons = doEmbedIcons;

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getEmbedAvatarsSetting() {
        return doEmbedAvatars;
    }

    @Override
    public void setEmbedAvatarsSetting(boolean doEmbedAvatars) {
        if (this.doEmbedAvatars != doEmbedAvatars) {
            this.doEmbedAvatars = doEmbedAvatars;

            hasSettingChanged = true;
        }
    }

    @Override
    public boolean getEmbedProjectAvatarsSetting() {
        return doEmbedProjectAvatars;
    }

    @Override
    public void setEmbedProjectAvatarsSetting(boolean doEmbedProjectAvatars) {
        if(this.doEmbedProjectAvatars != doEmbedProjectAvatars) {
            this.doEmbedProjectAvatars = doEmbedProjectAvatars;

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