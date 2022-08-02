package com.microsoft.teams.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

public interface AppSettings extends Entity {

    Boolean getEmbedIcons();

    void setEmbedIcons(Boolean embedIcons);

    Boolean getEmbedAvatars();

    void setEmbedAvatars(Boolean embedAvatars);

    Boolean getEmbedProjectAvatars();

    void setEmbedProjectAvatars(Boolean embedProjectAvatars);
}
