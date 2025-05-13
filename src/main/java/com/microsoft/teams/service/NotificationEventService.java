package com.microsoft.teams.service;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.watcher.WatchingDisabledException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.config.PluginSettings;
import com.microsoft.teams.service.models.notification.MsTeamsNotificationEvent;
import com.microsoft.teams.service.models.notification.NotificationEventChangeLog;
import com.microsoft.teams.service.models.notification.NotificationEventComment;
import com.microsoft.teams.service.models.notification.NotificationEventIssue;
import com.microsoft.teams.service.models.notification.NotificationEventType;
import com.microsoft.teams.service.models.notification.NotificationEventUser;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
    public static final String CHANGELOG_DESCRIPTION = "description";
    public static final int MAX_DATA_LENGTH = 500;
    private final TeamsAtlasUserService teamsAtlasUserService;
    private final SignalRService signalRService;
    private final AppKeysService appKeysService;
    private final PluginSettings pluginSettings;
    private final HostPropertiesService hostProperties;
    private MsTeamsNotificationEvent notificationEvent;

    @Autowired
    public NotificationEventService(
            TeamsAtlasUserService teamsAtlasUserService,
            SignalRService signalRService,
            AppKeysService appKeysService,
            PluginSettings pluginSettings, HostPropertiesService hostProperties) {
        this.teamsAtlasUserService = teamsAtlasUserService;
        this.signalRService = signalRService;
        this.appKeysService = appKeysService;
        this.pluginSettings = pluginSettings;
        this.hostProperties = hostProperties;
    }

    public MsTeamsNotificationEvent buildNotificationEvent(IssueEvent issueEvent) {
        try {
            LOG.trace("Processing issue event: {} for {}", issueEvent.getEventTypeId(), issueEvent.getIssue().getId());
            notificationEvent = new MsTeamsNotificationEvent();

            final ApplicationUser issueEventUser = issueEvent.getUser();

            notificationEvent.setJiraId(appKeysService.getAtlasId());
            notificationEvent.setEventType(getEventType(issueEvent));
            notificationEvent.setUser(buildNotificationEventUser(issueEventUser, issueEvent));
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

    private NotificationEventUser buildNotificationEventUser(ApplicationUser applicationUser, IssueEvent issueEvent) {
        if (applicationUser == null) {
            return null;
        }

        final NotificationEventUser notificationEventUser = new NotificationEventUser();
        final List<TeamsAtlasUser> teamsAtlasUsers = teamsAtlasUserService.getUserByUserName(applicationUser.getName());

        notificationEventUser.setId(applicationUser.getId().toString());
        notificationEventUser.setName(applicationUser.getDisplayName());
        if (!teamsAtlasUsers.isEmpty()) {
            notificationEventUser.setMicrosoftId(teamsAtlasUsers.get(0).getMsTeamsUserId());
            notificationEvent.incrementReceiversCount();

            // check view permissions only for connected users
            notificationEventUser.setCanViewIssue(canUserViewIssue(applicationUser, issueEvent.getIssue()));
            notificationEventUser.setCanViewComment(canUserViewComment(applicationUser, issueEvent.getComment()));
        }

        try {
            AvatarService avatarService = ComponentAccessor.getComponent(AvatarService.class);
            notificationEventUser.setAvatarUrl(avatarService.getAvatarURL(applicationUser, applicationUser).toString());
        } catch (Exception e) {
            LOG.trace("Cannot get avatar URL for user: {}", applicationUser.getName(), e);
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
                LOG.trace("Cannot get change log data from the event", e);
            }
        } else {
            return null;
        }

        return notificationEventChangeLogs;
    }

    private NotificationEventComment buildNotificationEventComment(IssueEvent issueEvent) {
        final NotificationEventComment notificationEventComment = new NotificationEventComment();
        final Comment comment = issueEvent.getComment();
        if(comment != null) {
            notificationEventComment.setContent(StringUtils.truncate(comment.getBody(), MAX_DATA_LENGTH));
            notificationEventComment.setInternal(isCommentInternal(issueEvent.getUser(), comment));

            return notificationEventComment;
        }
        return null;
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
        notificationEventIssue.setSelf(hostProperties.getFullBaseUrl() + "/browse/" + issue.getKey());

        notificationEventIssue.setAssignee(buildNotificationEventUser(issueAssignee, issueEvent));
        notificationEventIssue.setReporter(buildNotificationEventUser(issueReporter, issueEvent));

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
                        .map(watcher -> buildNotificationEventUser(watcher, issueEvent))
                        .collect(Collectors.toList());
                if(watchers.isEmpty()) {
                    continue; // try to get watchers for the next user
                }
                break; // Exit if watchers are successfully retrieved
            } catch (WatchingDisabledException e) {
                LOG.trace("Error while trying to get watchers with user: {}", user.getName(), e);
            }
        }

        return notificationEventWatchers;
    }

    private List<NotificationEventUser> getEventMentions(IssueEvent issueEvent) {
        List<NotificationEventUser> notificationEventMentions = new ArrayList<>();
        final NotificationEventChangeLog descriptionChangelog
                = this.notificationEvent.getChangelog() != null ?
                    this.notificationEvent.getChangelog()
                            .stream()
                            .filter(cl -> cl.getField().equals(CHANGELOG_DESCRIPTION))
                            .findFirst()
                            .orElse(null)
                : null;
        final String descriptionChangelogTo = descriptionChangelog != null ? descriptionChangelog.getTo() : "";
        final String issueComment = issueEvent.getComment() != null ? issueEvent.getComment().getBody() : null;

        final Pattern mentionPattern = Pattern.compile("\\[~(.*?)]");

        List<String> fieldsToCheck = Arrays.asList(descriptionChangelogTo, issueComment);
        for (String field : fieldsToCheck) {
            if (field != null) {
                Matcher mentionMatcher = mentionPattern.matcher(field);
                while (mentionMatcher.find()) {
                    String mention = mentionMatcher.group(1);

                    ApplicationUser mentionedUser = ComponentAccessor.getUserManager().getUserByName(mention);
                    if (mentionedUser != null) {
                        notificationEventMentions.add(buildNotificationEventUser(mentionedUser, issueEvent));
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

    private boolean isCommentInternal(ApplicationUser applicationUser, Comment comment) {
        try {
            CommentPropertyService propertyService
                    = ComponentAccessor.getComponent(CommentPropertyService.class);
            EntityPropertyService.PropertyResult propertyResult
                    = propertyService.getProperty(applicationUser, comment.getId(), "sd.public.comment");
            EntityProperty entityProperty = propertyResult.getEntityProperty().getOrNull();
            // if the property is not set, we assume it's not internal
            if (entityProperty == null) {
                return false;
            }
            String propertyValue = entityProperty.getValue();
            if (propertyValue != null) {
                return isCommentInternalPropertySet(propertyValue);
            }
        } catch (Exception e) {
            LOG.trace("Error while checking comment internal status", e);
        }
        return false;
    }

    private boolean isCommentInternalPropertySet(String propertyValue) {
        try {
            // check if the property value with key "internal" is set to true
            JSONObject jsonObject = new JSONObject(propertyValue);
            return jsonObject.optBoolean("internal", true);
        } catch (JSONException e) {
            LOG.trace("Error parsing entity property value as JSON: {}", propertyValue, e);
            return true;
        }
    }

    private boolean canUserViewComment(ApplicationUser user, Comment comment) {
        if (comment == null) {
            return false;
        }
        try {
            CommentPermissionManager permissionManager = ComponentAccessor.getComponent(CommentPermissionManager.class);
            boolean canViewComment = permissionManager.hasBrowsePermission(user, comment);
            if(isCommentInternal(user, comment)) {
                return canViewComment &&
                        isUserServiceDeskAgentForIssue(user, comment.getIssue());
            }
            return canViewComment;
        } catch (Exception e) {
            LOG.trace("Error while checking comment visibility", e);
            return false;
        }
    }

    private boolean canUserViewIssue(ApplicationUser user, Issue issue) {
        return hasPermissions("BROWSE_PROJECTS", user, issue);
    }

    private boolean isUserServiceDeskAgentForIssue(ApplicationUser user, Issue issue) {
        return hasPermissions("SERVICEDESK_AGENT", user, issue);
    }

    private boolean hasPermissions(String permissionKey, ApplicationUser user, Issue issue) {
        try {
            if (user == null || issue == null) {
                return false;
            }

            PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
            return permissionManager.hasPermission(new ProjectPermissionKey(permissionKey), issue, user);

        } catch (Exception e) {
            LOG.trace("Error while checking user permissions", e);
            return false;
        }
    }
}
