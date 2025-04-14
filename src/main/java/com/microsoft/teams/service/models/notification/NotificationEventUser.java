package com.microsoft.teams.service.models.notification;

public class NotificationEventUser {
    String id;
    String microsoftId;
    String name;
    String avatarUrl;

    public NotificationEventUser() {
    }
    public NotificationEventUser(String microsoftId) {
        this.microsoftId = microsoftId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMicrosoftId() {
        return microsoftId;
    }

    public void setMicrosoftId(String microsoftId) {
        this.microsoftId = microsoftId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "NotificationEventUser{" +
                "id='" + id + '\'' +
                ", microsoftId='" + microsoftId + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}