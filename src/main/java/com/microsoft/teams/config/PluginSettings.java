package com.microsoft.teams.config;

public interface PluginSettings {

    boolean getEmbedIconsSetting();

    void setEmbedIconsSetting(boolean doEmbedIcons);

    boolean getEmbedAvatarsSetting();

    void setEmbedAvatarsSetting(boolean doEmbedAvatars);

    boolean getEmbedProjectAvatarsSetting();

    void setEmbedProjectAvatarsSetting(boolean doEmbedAvatars);

    boolean getPersonalNotificationsSetting();

    void setPersonalNotificationsSetting(boolean doPersonalNotifications);

    boolean getGroupNotificationsSetting();

    void setGroupNotificationsSetting(boolean doGroupNotifications);

    boolean hasChanged();

    void resetObservableState();
}
