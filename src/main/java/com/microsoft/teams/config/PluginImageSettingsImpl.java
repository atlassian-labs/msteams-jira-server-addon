package com.microsoft.teams.config;

import org.springframework.stereotype.Component;

@Component
public class PluginImageSettingsImpl implements PluginImageSettings{

    // Product decision - embed icons by default
    private boolean doEmbedIcons = true;
    private boolean doEmbedAvatars;
    private boolean doEmbedProjectAvatars = true;

    @Override
    public boolean getEmbedIconsSetting() {
        return doEmbedIcons;
    }

    @Override
    public void setEmbedIconsSetting(boolean doEmbedIcons) {
        this.doEmbedIcons = doEmbedIcons;
    }

    @Override
    public boolean getEmbedAvatarsSetting() {
        return doEmbedAvatars;
    }

    @Override
    public void setEmbedAvatarsSetting(boolean doEmbedAvatars) {
        this.doEmbedAvatars = doEmbedAvatars;
    }

    @Override
    public boolean getEmbedProjectAvatarsSetting() {
        return doEmbedProjectAvatars;
    }

    @Override
    public void setEmbedProjectAvatarsSetting(boolean doEmbedProjectAvatars) {
        this.doEmbedProjectAvatars = doEmbedProjectAvatars;
    }
}