package com.microsoft.teams.listener.dto;

import java.util.List;

public class MsTeamsEvent {

    String jiraServerId; //atlasId

    List<MsTeamsUserId> receivers;

    String issueKey;

    String issueSummary;

    String eventUserName;

    String issueId;

    String issueProject;

    IssueEventType eventType;

    List<IssueField> issueFields;

    public String getJiraServerId() {
        return jiraServerId;
    }

    public void setJiraServerId(String jiraServerId) {
        this.jiraServerId = jiraServerId;
    }

    public List<MsTeamsUserId> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<MsTeamsUserId> receivers) {
        this.receivers = receivers;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public String getEventUserName() {
        return eventUserName;
    }

    public void setEventUserName(String eventUserName) {
        this.eventUserName = eventUserName;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getIssueProject() {
        return issueProject;
    }

    public void setIssueProject(String issueProject) {
        this.issueProject = issueProject;
    }

    public IssueEventType getEventType() {
        return eventType;
    }

    public void setEventType(IssueEventType eventType) {
        this.eventType = eventType;
    }

    public List<IssueField> getIssueFields() {
        return issueFields;
    }

    public void setIssueFields(List<IssueField> issueFields) {
        this.issueFields = issueFields;
    }

    @Override
    public String toString() {
        return "MsTeamsEvent{" +
                "jiraServerId='" + jiraServerId + '\'' +
                ", receivers=" + receivers +
                ", issueKey='" + issueKey + '\'' +
                ", issueSummary='" + issueSummary + '\'' +
                ", eventUserName='" + eventUserName + '\'' +
                ", issueId='" + issueId + '\'' +
                ", issueProject='" + issueProject + '\'' +
                ", eventType=" + eventType +
                ", issueFields=" + issueFields +
                '}';
    }
}
