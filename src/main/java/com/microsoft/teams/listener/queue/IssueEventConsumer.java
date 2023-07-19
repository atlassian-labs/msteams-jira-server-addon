package com.microsoft.teams.listener.queue;

import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.lang.Pair;
import com.google.gson.Gson;
import com.microsoft.teams.ao.TeamsAtlasUser;
import com.microsoft.teams.listener.dto.IssueEventType;
import com.microsoft.teams.listener.dto.IssueField;
import com.microsoft.teams.listener.dto.MsTeamsEvent;
import com.microsoft.teams.listener.dto.MsTeamsUserId;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.AppPropertiesService;
import com.microsoft.teams.service.TeamsAtlasUserService;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

public class IssueEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(IssueEventConsumer.class);

    private static final int POLL_FREQUENCY = 1000;

    private final Queue<IssueEvent> queue;
    private final TeamsAtlasUserService teamsAtlasUserService;
    private final AppKeysService appKeysService;
    private final AppPropertiesService appProperties;

    public IssueEventConsumer(Queue<IssueEvent> queue,
                              TeamsAtlasUserService teamsAtlasUserService,
                              AppKeysService appKeysService,
                              AppPropertiesService appProperties) {
        this.queue = queue;
        this.teamsAtlasUserService = teamsAtlasUserService;
        this.appKeysService = appKeysService;
        this.appProperties = appProperties;
    }

    public void consume() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            IssueEvent poll;
            while (!Objects.isNull(poll = queue.poll())) {
                try{
                    processIssueEvent(poll);
                } catch(Exception e) {
                        exceptionLogExtender("consume() cannot process event ", Level.ERROR, e);
                    }
                }
            }, 0, POLL_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    private void processIssueEvent(IssueEvent issueEvent) {
        final MsTeamsEvent msTeamsEvent = new MsTeamsEvent();
        final ApplicationUser issueEventUser = issueEvent.getUser();
        final String username = Objects.isNull(issueEventUser) ? null : issueEventUser.getUsername();
        final List<TeamsAtlasUser> teamsAtlasUsers = teamsAtlasUserService.getUserByUserName(username);
        final Set<String> userNames = new HashSet<>();
        final Issue issue = issueEvent.getIssue();

        if (!Objects.isNull(issue.getAssignee())) {
            userNames.add(issue.getAssignee().getUsername());
        }

        if (!teamsAtlasUsers.isEmpty()) {
            ApplicationUser eventUser = ComponentAccessor.getUserManager().getUserByName(teamsAtlasUsers.get(0).getUserName());
            WatcherService watcherService = ComponentAccessor.getComponent(WatcherService.class);
            final Pair<Integer, List<ApplicationUser>> watchers = watcherService.getWatchers(issue, eventUser).get();
            final List<String> watcherNames = watchers.second().stream()
                    .map(ApplicationUser::getName)
                    .collect(Collectors.toList());

            userNames.addAll(watcherNames);
        }

        final IssueEventType eventType = getEventType(issueEvent);

        LOG.info("Process event {} for issue {} and user {}", eventType, issue, username);

        if (!eventType.equals(IssueEventType.UNKNOWN)) {
            msTeamsEvent.setJiraServerId(appKeysService.getAtlasId());
            final List<MsTeamsUserId> receivers = userNames.stream()
                    .filter(userName -> !username.equals(userName))
                    .map(teamsAtlasUserService::getUserByUserName)
                    .filter(list -> list.size() > 0)
                    .map(atlasUsers -> new MsTeamsUserId(atlasUsers.get(0).getMsTeamsUserId()))
                    .collect(Collectors.toList());

            if (receivers.size() > 0) {
                if (eventType.equals(IssueEventType.ISSUE_UPDATED)) {
                    try {
                        processIssueUpdate(issueEvent, msTeamsEvent);
                    } catch (GenericEntityException e) {
                        LOG.info("Can't process issue update, error({}): {}", e.getClass().getName(), e.getMessage());
                    }
                } else {
                    msTeamsEvent.setEventType(eventType);
                }
                msTeamsEvent.setReceivers(receivers);
                msTeamsEvent.setIssueKey(issue.getKey());
                msTeamsEvent.setIssueSummary(issue.getSummary());
                msTeamsEvent.setIssueId(String.valueOf(issue.getId()));
                if (!Objects.isNull(issue.getProjectObject())) {
                    msTeamsEvent.setIssueProject(issue.getProjectObject().getName());
                }
                if (!Objects.isNull(issueEventUser)) {
                    msTeamsEvent.setEventUserName(issueEventUser.getDisplayName());
                }

                notifyMSTeams(msTeamsEvent);
            }
        }
    }

    private void notifyMSTeams(MsTeamsEvent msTeamsEvent) {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(appProperties.getTeamsAppBaseUrl() + "/api/notifications/feedEvent");
        try {
            StringEntity entity = new StringEntity(new Gson().toJson(msTeamsEvent));
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            LOG.trace("Send notify object {} to MSTeams", msTeamsEvent);
            client.execute(httpPost);
            client.close();
        } catch (Exception e) {
            LOG.info("Can't send notification to MSTeams, error({}): {}", e.getClass().getName(), e.getMessage());
        }

    }

    private IssueEventType getEventType(IssueEvent issueEvent) {
        IssueEventType eventType;
        Long eventTypeId = issueEvent.getEventTypeId();
        if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)) {
            eventType = IssueEventType.ISSUE_ASSIGNED;
        } else if (eventTypeId.equals(EventType.ISSUE_GENERICEVENT_ID)) {
            eventType = IssueEventType.ISSUE_GENERIC;
        } else if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID)) {
            eventType = IssueEventType.COMMENT_CREATED;
        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
            eventType = IssueEventType.ISSUE_UPDATED;
        } else {
            eventType = IssueEventType.UNKNOWN;
        }

        return eventType;
    }

    private void processIssueUpdate(IssueEvent issueEvent, MsTeamsEvent msTeamsEvent) throws GenericEntityException {
        final GenericValue changeLog = issueEvent.getChangeLog();
        if (Objects.isNull(changeLog)) {
            if (!Objects.isNull(issueEvent.getComment())) {
                msTeamsEvent.setEventType(IssueEventType.COMMENT_CREATED);
            } else {
                msTeamsEvent.setEventType(IssueEventType.UNKNOWN);
            }
        } else {
            final List<GenericValue> childChangeItem = changeLog.getRelated("ChildChangeItem");
            if (childChangeItem.size() == 1 && childChangeItem.get(0).get("field").equals("assignee")) {
                msTeamsEvent.setEventType(IssueEventType.ISSUE_ASSIGNED);
            } else {
                final List<IssueField> fields = childChangeItem.stream()
                        .map(t -> new IssueField(String.valueOf(t.get("field"))))
                        .collect(Collectors.toList());
                msTeamsEvent.setEventType(IssueEventType.ISSUE_UPDATED);
                msTeamsEvent.setIssueFields(fields);
            }
        }
    }

}
