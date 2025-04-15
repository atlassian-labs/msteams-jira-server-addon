package com.microsoft.teams.service;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.watcher.WatchingDisabledException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.config.PluginSettings;
import com.microsoft.teams.service.models.notification.MsTeamsNotificationEvent;
import com.microsoft.teams.service.models.notification.NotificationEventChangeLog;
import com.microsoft.teams.service.models.notification.NotificationEventComment;
import com.microsoft.teams.service.models.notification.NotificationEventIssue;
import com.microsoft.teams.service.models.notification.NotificationEventType;
import com.microsoft.teams.service.models.notification.NotificationEventUser;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class NotificationEventService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventService.class);
    public static final String CHANGELOG_ASSIGNEE = "assignee";
    private final TeamsAtlasUserService teamsAtlasUserService;
    private final SignalRService signalRService;
    private final AppKeysService appKeysService;
    private final PluginSettings pluginSettings;
    private MsTeamsNotificationEvent notificationEvent;

    @Autowired
    public NotificationEventService(
            TeamsAtlasUserService teamsAtlasUserService,
            SignalRService signalRService,
            AppKeysService appKeysService,
            PluginSettings pluginSettings) {
        this.teamsAtlasUserService = teamsAtlasUserService;
        this.signalRService = signalRService;
        this.appKeysService = appKeysService;
        this.pluginSettings = pluginSettings;
    }

    public MsTeamsNotificationEvent buildNotificationEvent(IssueEvent issueEvent) {
        try {
            LOG.trace("Processing issue event: {} for {}", issueEvent.getEventTypeId(), issueEvent.getIssue().getId());
            notificationEvent = new MsTeamsNotificationEvent();

            final ApplicationUser issueEventUser = issueEvent.getUser();

            notificationEvent.setJiraId(appKeysService.getAtlasId());
            notificationEvent.setEventType(getEventType(issueEvent));
            notificationEvent.setUser(buildNotificationEventUser(issueEventUser));
            notificationEvent.setIssue(buildNotificationEventIssue(issueEvent));
            notificationEvent.setChangelog(buildNotificationEventChangeLog(issueEvent));
            notificationEvent.setComment(buildNotificationEventComment(issueEvent));
            notificationEvent.setWatchers(getIssueWatchers(issueEvent));
            notificationEvent.setMentions(getEventMentions(issueEvent));

            return notificationEvent;
        } catch (Exception e) {
            LOG.error("Error while building notification event", e);
            return null;
        }
    }

    public void notifyMSTeams() {
        if(this.notificationEvent != null) {

            if(!pluginSettings.getGroupNotificationsSetting() &&
                    (this.notificationEvent.getReceiversCount() == 0 && pluginSettings.getPersonalNotificationsSetting())) {
                LOG.debug("Skip sending notification event: {}. Group notifications are not configured or no potential receivers for personal notifications found.",
                        this.notificationEvent);
                return;
            }
            LOG.trace("Send notify object {} to MSTeams", notificationEvent);
            this.signalRService.sendNotificationEvent(notificationEvent);
        }
    }

    private NotificationEventUser buildNotificationEventUser(ApplicationUser applicationUser) {
        if (applicationUser == null) {
            return null;
        }

        final NotificationEventUser notificationEventUser = new NotificationEventUser();
        final String userName = applicationUser.getName();
        final List<TeamsAtlasUser> teamsAtlasUsers = teamsAtlasUserService.getUserByUserName(userName);

        notificationEventUser.setId(applicationUser.getId().toString());
        notificationEventUser.setName(userName);
        if (!teamsAtlasUsers.isEmpty()) {
            notificationEventUser.setMicrosoftId(teamsAtlasUsers.get(0).getMsTeamsUserId());
            notificationEvent.incrementReceiversCount();
        }

        try {
            AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
            notificationEventUser.setAvatarUrl(avatarService.getAvatarURL(applicationUser, applicationUser).toString());
        } catch (Exception e) {
            LOG.warn("Cannot get avatar URL for user: {}", applicationUser.getName(), e);
        }

        return notificationEventUser;
    }

    private List<NotificationEventChangeLog> buildNotificationEventChangeLog(IssueEvent issueEvent) {
        final List<NotificationEventChangeLog> notificationEventChangeLogs = new ArrayList<>();
        final GenericValue changeLog = issueEvent.getChangeLog();
        if (!Objects.isNull(changeLog)) {
            try {
                final List<GenericValue> childChangeItems = changeLog.getRelated("ChildChangeItem");
                for (final GenericValue changeItem : childChangeItems) {
                    final String field = changeItem.getString("field");
                    final String oldValue = changeItem.getString("oldstring");
                    final String newValue = changeItem.getString("newstring");

                    if (field != null && !field.isEmpty()) {
                        final NotificationEventChangeLog notificationEventChangeLog = new NotificationEventChangeLog();
                        notificationEventChangeLog.setField(field);
                        notificationEventChangeLog.setFrom(oldValue);
                        notificationEventChangeLog.setTo(newValue);

                        notificationEventChangeLogs.add(notificationEventChangeLog);

                        if (field.equals(CHANGELOG_ASSIGNEE)) {
                            notificationEvent.setEventType(NotificationEventType.ISSUE_ASSIGNED);
                        }
                    }
                }

            } catch (GenericEntityException e) {
                LOG.error("Cannot get change log data from the event", e);
            }
        } else {
            return null;
        }

        return notificationEventChangeLogs;
    }

    private NotificationEventComment buildNotificationEventComment(IssueEvent issueEvent) {
        final NotificationEventComment notificationEventComment = new NotificationEventComment();
        if(issueEvent.getComment() != null) {
            notificationEventComment.setContent(issueEvent.getComment().getBody());
        }
        return notificationEventComment;
    }

    private NotificationEventIssue buildNotificationEventIssue(IssueEvent issueEvent) {
        final NotificationEventIssue notificationEventIssue = new NotificationEventIssue();
        final Issue issue = issueEvent.getIssue();

        final ApplicationUser issueAssignee = issue.getAssignee();
        final ApplicationUser issueReporter = issue.getReporter();

        notificationEventIssue.setId(issue.getId().toString());
        notificationEventIssue.setKey(issue.getKey());
        notificationEventIssue.setSummary(issue.getSummary());
        notificationEventIssue.setStatus(issue.getStatus().getName());

        notificationEventIssue.setAssignee(buildNotificationEventUser(issueAssignee));
        notificationEventIssue.setReporter(buildNotificationEventUser(issueReporter));

        if(Objects.nonNull(issue.getIssueType())) {
            notificationEventIssue.setType(issue.getIssueType().getName());
        }

        if(Objects.nonNull(issue.getPriority())) {
            notificationEventIssue.setPriority(issue.getPriority().getName());
        }

        if (Objects.nonNull(issue.getProjectId())) {
            notificationEventIssue.setProjectID(issue.getProjectId().toString());
        }

        return notificationEventIssue;
    }

    private List<NotificationEventUser> getIssueWatchers(IssueEvent issueEvent) {
        List<NotificationEventUser> notificationEventWatchers = new ArrayList<>();
        final Issue issue = issueEvent.getIssue();
        final ApplicationUser issueEventUser = issueEvent.getUser();
        final ApplicationUser issueAssignee = issue.getAssignee();
        final ApplicationUser issueReporter = issue.getReporter();

        WatcherService watcherService = ComponentAccessor.getComponent(WatcherService.class);

        List<ApplicationUser> usersToGetWatchers = Arrays.asList(issueReporter, issueEventUser, issueAssignee);

        for (ApplicationUser user : usersToGetWatchers) {
            if (user == null) {
                continue;
            }
            try {
                List<ApplicationUser> watchers = watcherService.getWatchers(issue, user).get().second();
                notificationEventWatchers = watchers.stream()
                        .map(this::buildNotificationEventUser)
                        .collect(Collectors.toList());
                break; // Exit if watchers are successfully retrieved
            } catch (WatchingDisabledException e) {
                LOG.warn("Error while trying to get watchers with user: {}", user.getName(), e);
            }
        }

        return notificationEventWatchers;
    }

    private List<NotificationEventUser> getEventMentions(IssueEvent issueEvent) {
        List<NotificationEventUser> notificationEventMentions = new ArrayList<>();
        final Issue issue = issueEvent.getIssue();
        final String issueDescription = issue.getDescription();
        final String issueComment = issueEvent.getComment() != null ? issueEvent.getComment().getBody() : null;

        final Pattern mentionPattern = Pattern.compile("\\[~(.*?)]");

        List<String> fieldsToCheck = Arrays.asList(issueDescription, issueComment);
        for (String field : fieldsToCheck) {
            if (field != null) {
                Matcher mentionMatcher = mentionPattern.matcher(field);
                while (mentionMatcher.find()) {
                    String mention = mentionMatcher.group(1);

                    ApplicationUser mentionedUser = ComponentAccessor.getUserManager().getUserByKey(mention);
                    if (mentionedUser != null) {
                        notificationEventMentions.add(buildNotificationEventUser(mentionedUser));
                    }
                }
            }
        }

        return notificationEventMentions;
    }

    private NotificationEventType getEventType(IssueEvent issueEvent) {
        NotificationEventType eventType;
        Long eventTypeId = issueEvent.getEventTypeId();
        if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)) {
            eventType = NotificationEventType.ISSUE_ASSIGNED;
        } else if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID)) {
            eventType = NotificationEventType.COMMENT_CREATED;
        } else if (eventTypeId.equals(EventType.ISSUE_COMMENT_EDITED_ID)) {
            eventType = NotificationEventType.COMMENT_UPDATED;
        } else if (eventTypeId.equals(EventType.ISSUE_COMMENT_DELETED_ID)) {
            eventType = NotificationEventType.COMMENT_DELETED;
        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)
                || eventTypeId.equals(EventType.ISSUE_RESOLVED_ID)
                || eventTypeId.equals(EventType.ISSUE_CLOSED_ID)
                || eventTypeId.equals(EventType.ISSUE_REOPENED_ID)
                || eventTypeId.equals(EventType.ISSUE_WORKLOGGED_ID)
                || eventTypeId.equals(EventType.ISSUE_WORKSTARTED_ID)
                || eventTypeId.equals(EventType.ISSUE_WORKSTOPPED_ID)
                || eventTypeId.equals(EventType.ISSUE_WORKLOG_UPDATED_ID)
                || eventTypeId.equals(EventType.ISSUE_WORKLOG_DELETED_ID)
                || eventTypeId.equals(EventType.ISSUE_GENERICEVENT_ID)) {
            eventType = NotificationEventType.ISSUE_UPDATED;
        } else if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            eventType = NotificationEventType.ISSUE_CREATED;
        } else {
            eventType = NotificationEventType.UNKNOWN;
        }

        return eventType;
    }

}
