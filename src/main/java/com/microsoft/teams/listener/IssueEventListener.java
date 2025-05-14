package com.microsoft.teams.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.google.common.collect.ImmutableList;
import com.microsoft.teams.config.PluginSettings;
import com.microsoft.teams.listener.queue.IssueEventConsumer;
import com.microsoft.teams.service.NotificationEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

@Component
public class IssueEventListener implements InitializingBean, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(IssueEventListener.class);

    private final List<Long> eventTypes = ImmutableList.of(
            EventType.ISSUE_ASSIGNED_ID,
            EventType.ISSUE_COMMENTED_ID,
            EventType.ISSUE_COMMENT_EDITED_ID,
            EventType.ISSUE_COMMENT_DELETED_ID,
            EventType.ISSUE_UPDATED_ID,
            EventType.ISSUE_RESOLVED_ID,
            EventType.ISSUE_CLOSED_ID,
            EventType.ISSUE_REOPENED_ID,
            EventType.ISSUE_WORKLOGGED_ID,
            EventType.ISSUE_WORKSTARTED_ID,
            EventType.ISSUE_WORKSTOPPED_ID,
            EventType.ISSUE_WORKLOG_UPDATED_ID,
            EventType.ISSUE_WORKLOG_DELETED_ID,
            EventType.ISSUE_GENERICEVENT_ID,
            EventType.ISSUE_CREATED_ID);
    private final Queue<IssueEvent> queue = new ConcurrentLinkedQueue<IssueEvent>();
    private final NotificationEventService notificationEventService;
    private final PluginSettings pluginSettings;

    @JiraImport
    private final EventPublisher eventPublisher;

    @Autowired
    public IssueEventListener(EventPublisher eventPublisher,
                              NotificationEventService notificationEventService, PluginSettings pluginSettings) {
        this.eventPublisher = eventPublisher;
        this.notificationEventService = notificationEventService;
        this.pluginSettings = pluginSettings;
    }

    /**
     * Called when the plugin has been enabled.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            eventPublisher.register(this);
            IssueEventConsumer issueEventConsumer = new IssueEventConsumer(queue, notificationEventService);
            issueEventConsumer.consume();
            LOG.info("afterPropertiesSet() info: Issue event listener has been successfully registered and started");
        } catch (Exception e) {
            exceptionLogExtender("afterPropertiesSet() error ", Level.ERROR, e);
        }
    }

    /**
     * Called when the plugin is being disabled or removed.
     */
    @Override
    public void destroy() {
        LOG.info("Disabling IssueEventListener plugin");
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        boolean areNotificationsEnabled = pluginSettings.getPersonalNotificationsSetting()
                || pluginSettings.getGroupNotificationsSetting() ;
        if (eventTypes.contains(issueEvent.getEventTypeId()) && areNotificationsEnabled) {
            LOG.trace("Event type {} added to queue.", issueEvent.getEventTypeId());
            queue.add(issueEvent);
        } else {
            LOG.trace("Not supported event type: {} or notifications are disabled for the plugin", issueEvent.getEventTypeId());
        }
    }
}
