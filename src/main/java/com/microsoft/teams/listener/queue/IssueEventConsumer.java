package com.microsoft.teams.listener.queue;

import com.atlassian.jira.event.issue.IssueEvent;
import com.microsoft.teams.service.NotificationEventService;
import com.microsoft.teams.service.models.notification.MsTeamsNotificationEvent;
import org.slf4j.event.Level;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.microsoft.teams.utils.ExceptionHelpers.exceptionLogExtender;

public class IssueEventConsumer {

    private static final int POLL_FREQUENCY = 100;

    private final Queue<IssueEvent> queue;
    private final NotificationEventService notificationEventService;

    public IssueEventConsumer(Queue<IssueEvent> queue,
                              NotificationEventService notificationEventService) {
        this.queue = queue;
        this.notificationEventService = notificationEventService;
    }

    public void consume() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            IssueEvent poll;
            while (!Objects.isNull(poll = queue.poll())) {
                try {
                    processIssueEvent(poll);
                } catch (Exception e) {
                        exceptionLogExtender("consume() cannot process event ", Level.ERROR, e);
                    }
                }
            }, 0, POLL_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    private void processIssueEvent(IssueEvent issueEvent) {
        try {
            final MsTeamsNotificationEvent msTeamsNotificationEvent
                    = notificationEventService.buildNotificationEvent(issueEvent);

            if (msTeamsNotificationEvent != null) {
                notificationEventService.notifyMSTeams();
            }
        } catch (Exception e) {
            exceptionLogExtender("processIssueEvent() cannot process event ", Level.ERROR, e);
        }
    }
}
