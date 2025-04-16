package com.microsoft.teams.service.models.notification;

public class NotificationEventComment {
    String content;

    boolean isInternal;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    @Override
    public String toString() {
        return "NotificationEventComment{" +
                "content='" + content + '\'' +
                ", isInternal=" + isInternal +
                '}';
    }
}
