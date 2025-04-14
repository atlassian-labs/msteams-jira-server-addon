package com.microsoft.teams.service.models.notification;

public class NotificationEventComment {
    String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "NotificationEventComment{" +
                "content='" + content + '\'' +
                '}';
    }
}
