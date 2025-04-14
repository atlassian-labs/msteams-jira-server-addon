package com.microsoft.teams.service.models.notification;

public class NotificationEventIssue {
    String id;
    String key;
    String summary;
    String status;
    String type;
    NotificationEventUser assignee;
    NotificationEventUser reporter;
    String priority;
    String self;
    String projectID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NotificationEventUser getAssignee() {
        return assignee;
    }

    public void setAssignee(NotificationEventUser assignee) {
        this.assignee = assignee;
    }

    public NotificationEventUser getReporter() {
        return reporter;
    }

    public void setReporter(NotificationEventUser reporter) {
        this.reporter = reporter;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    @Override
    public String toString() {
        return "NotificationEventIssue{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", summary='" + summary + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", assignee=" + assignee +
                ", reporter=" + reporter +
                ", priority='" + priority + '\'' +
                ", self='" + self + '\'' +
                ", projectID='" + projectID + '\'' +
                '}';
    }
}
