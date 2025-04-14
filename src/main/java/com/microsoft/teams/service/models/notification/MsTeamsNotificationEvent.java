package com.microsoft.teams.service.models.notification;

import java.util.List;

public class MsTeamsNotificationEvent {
    String jiraId; // Jira Connection ID
    NotificationEventType eventType;
    NotificationEventUser user;
    NotificationEventIssue issue;
    List<NotificationEventChangeLog> changelog;
    NotificationEventComment comment;
    List<NotificationEventUser> watchers;
    List<NotificationEventUser> mentions;

    int receiversCount = 0;

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public String getJiraId() {
        return jiraId;
    }

    public void setJiraId(String jiraId) {
        this.jiraId = jiraId;
    }

    public NotificationEventUser getUser() {
        return user;
    }

    public void setUser(NotificationEventUser user) {
        this.user = user;
    }

    public NotificationEventIssue getIssue() {
        return issue;
    }

    public void setIssue(NotificationEventIssue issue) {
        this.issue = issue;
    }

    public List<NotificationEventChangeLog> getChangelog() {
        return changelog;
    }

    public void setChangelog(List<NotificationEventChangeLog> changelog) {
        this.changelog = changelog;
    }

    public NotificationEventComment getComment() {
        return comment;
    }

    public void setComment(NotificationEventComment comment) {
        this.comment = comment;
    }

    public List<NotificationEventUser> getWatchers() {
        return watchers;
    }

    public void setWatchers(List<NotificationEventUser> watchers) {
        this.watchers = watchers;
    }

    public List<NotificationEventUser> getMentions() {
        return mentions;
    }

    public void setMentions(List<NotificationEventUser> mentions) {
        this.mentions = mentions;
    }

    public int getReceiversCount() {
        return receiversCount;
    }

    public void incrementReceiversCount() {
        this.receiversCount++;
    }

    @Override
    public String toString() {
        return "MsTeamsNotificationEvent{" +
                ", jiraId='" + jiraId + '\'' +
                ", eventType=" + eventType +
                ", user=" + user +
                ", issue=" + issue +
                ", changelog=" + changelog +
                ", comment=" + comment +
                ", watchers=" + watchers +
                ", mentions=" + mentions +
                '}';
    }
}
