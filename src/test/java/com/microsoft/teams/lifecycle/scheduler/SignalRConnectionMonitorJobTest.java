package com.microsoft.teams.lifecycle.scheduler;

import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.microsoft.teams.service.SignalRService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link com.microsoft.teams.lifecycle.scheduler.SignalRConnectionMonitorJob}
 */
@RunWith(MockitoJUnitRunner.class)
public class SignalRConnectionMonitorJobTest {
    private SignalRConnectionMonitorJob signalRConnectionMonitorJob;
    @Mock
    private SchedulerService scheduler;
    @Mock
    private SignalRService signalRService;
    @Mock
    private JobRunnerRequest jobRunnerRequest;

    @Before
    public void setUp() {
        signalRConnectionMonitorJob = new SignalRConnectionMonitorJob(scheduler, signalRService);
    }

    @Test
    public void testRegisterScheduler() throws SchedulerServiceException {
        signalRConnectionMonitorJob.registerScheduler();
        verify(scheduler, times(1)).scheduleJob(any(JobId.class), any(JobConfig.class));
    }

    @Test
    public void testRunJob() {
        signalRConnectionMonitorJob.runJob(jobRunnerRequest);
        verify(signalRService, times(1)).startSignalRConnection();
    }

    @Test
    public void testUnscheduleJob() {
        signalRConnectionMonitorJob.registerScheduler();
        signalRConnectionMonitorJob.destroy();
        verify(scheduler).unscheduleJob(any(JobId.class));
    }
}
