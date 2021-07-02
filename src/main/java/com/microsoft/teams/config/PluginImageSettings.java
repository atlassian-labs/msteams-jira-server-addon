package com.microsoft.teams.config;

public interface PluginImageSettings {

    boolean getEmbedIconsSetting();

    void setEmbedIconsSetting(boolean doEmbedIcons);

    boolean getEmbedAvatarsSetting();

    void setEmbedAvatarsSetting(boolean doEmbedAvatars);

    boolean getEmbedProjectAvatarsSetting();

    void setEmbedProjectAvatarsSetting(boolean doEmbedAvatars);

}
