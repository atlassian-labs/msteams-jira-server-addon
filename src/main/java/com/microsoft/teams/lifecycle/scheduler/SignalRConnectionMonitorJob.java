package com.microsoft.teams.lifecycle.scheduler;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.*;
import com.atlassian.scheduler.config.*;
import com.microsoft.teams.service.SignalRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class SignalRConnectionMonitorJob implements JobRunner, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SignalRConnectionMonitorJob.class);

    private static final long EVERY_MINUTE = TimeUnit.MINUTES.toMillis(1);
    private JobRunnerKey jobRunnerKey;
    private JobId jobId;

    private final SchedulerService scheduler;
    private final SignalRService signalRService;

    @Autowired
    public SignalRConnectionMonitorJob(@ComponentImport SchedulerService scheduler,
                                       SignalRService signalRService) {
        this.scheduler = scheduler;
        this.signalRService = signalRService;        
    }

    @Nullable
    @Override
    public JobRunnerResponse runJob(JobRunnerRequest jobRunnerRequest) {
        LOG.info("Running SignalRConnectionMonitorJob at {}", jobRunnerRequest.getStartTime());
        signalRService.startSignalRConnection();
        return JobRunnerResponse.success();
    }

    public void registerScheduler() {
        String id = SignalRConnectionMonitorJob.class.getName() + UUID.randomUUID().toString().replace("-", "");
        this.jobId = JobId.of(id);
        this.jobRunnerKey = JobRunnerKey.of(id);
        LOG.info("Registering SignalR Connection Monitor Job: " + id);

        scheduler.registerJobRunner(this.jobRunnerKey, this);
        final JobConfig jobConfig = JobConfig.forJobRunnerKey(this.jobRunnerKey)
                .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
                .withSchedule(Schedule.forInterval(EVERY_MINUTE, null));
        try {
            scheduler.scheduleJob(this.jobId, jobConfig);
            LOG.info("Scheduling SignalR Connection Monitor Job: " + id);
        } catch (SchedulerServiceException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void destroy() {
        LOG.info("Destroying SignalR Connection Monitor Job: " + this.jobId.toString());
        scheduler.unscheduleJob(this.jobId);
    }
}
