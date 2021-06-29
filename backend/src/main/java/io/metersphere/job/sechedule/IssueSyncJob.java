package io.metersphere.job.sechedule;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.track.service.IssuesService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class IssueSyncJob extends MsScheduleJob {

    private IssuesService issuesService;

    public IssueSyncJob() {
        issuesService = CommonBeanFactory.getBean(IssuesService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        issuesService.syncThirdPartyIssues();
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, ScheduleGroup.ISSUE_SYNC.name());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, ScheduleGroup.ISSUE_SYNC.name());
    }
}

