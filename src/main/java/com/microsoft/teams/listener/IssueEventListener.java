package com.microsoft.teams.listener;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.google.common.collect.ImmutableList;
import com.microsoft.teams.service.AppKeysService;
import com.microsoft.teams.service.AppPropertiesService;
import com.microsoft.teams.service.TeamsAtlasUserService;
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
            EventType.ISSUE_GENERICEVENT_ID,
            EventType.ISSUE_COMMENTED_ID,
            EventType.ISSUE_UPDATED_ID);
    private final Queue<IssueEvent> queue = new ConcurrentLinkedQueue<IssueEvent>();
    private final TeamsAtlasUserService teamsAtlasUserService;
    private final AppKeysService appKeysService;
    private final AppPropertiesService appProperties;

    @JiraImport
    private final EventPublisher eventPublisher;

    @Autowired
    public IssueEventListener(EventPublisher eventPublisher,
                              TeamsAtlasUserService teamsAtlasUserService,
                              AppKeysService appKeysService,
                              AppPropertiesService appProperties) {
        this.eventPublisher = eventPublisher;
        this.teamsAtlasUserService = teamsAtlasUserService;
        this.appKeysService = appKeysService;
        this.appProperties = appProperties;
    }

    /**
     * Called when the plugin has been enabled.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            // Uncomment this part of the code if you want to listen to Jira events
            /*
            eventPublisher.register(this);
            IssueEventConsumer issueEventConsumer = new IssueEventConsumer(queue, teamsAtlasUserService, appKeysService, appProperties);
            issueEventConsumer.consume();
            LOG.info("afterPropertiesSet() info: Issue event listener has been successfully registered and started");
            */
        } catch (Exception e) {
            exceptionLogExtender("afterPropertiesSet() error ", Level.ERROR, e);
        }
    }

    /**
     * Called when the plugin is being disabled or removed.
     */
    @Override
    public void destroy() {
        // Uncomment this part of the code if you want to listen to Jira events
        /*
        LOG.info("Disabling plugin");
        eventPublisher.unregister(this);
         */
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        if (eventTypes.contains(issueEvent.getEventTypeId())) {
            LOG.trace("Event {} added to queue.", issueEvent.getEventTypeId());
            queue.add(issueEvent);
        }
    }
}
