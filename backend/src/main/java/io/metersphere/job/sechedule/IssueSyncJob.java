package io.metersphere.job.sechedule;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.ProjectService;
import io.metersphere.track.service.IssuesService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.List;

public class IssueSyncJob extends MsScheduleJob {

    private IssuesService issuesService;
    private ProjectService projectService;

    public IssueSyncJob() {
        issuesService = CommonBeanFactory.getBean(IssuesService.class);
        projectService = CommonBeanFactory.getBean(ProjectService.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        List<String> projectIds = projectService.getThirdPartProjectIds();
        projectIds.forEach(id -> {
            try {
                // 一个项目开启一个事务，
                issuesService.syncThirdPartyIssues(id);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
    }

    public static JobKey getJobKey(String projectId) {
        return new JobKey(projectId, ScheduleGroup.ISSUE_SYNC.name());
    }

    public static TriggerKey getTriggerKey(String projectId) {
        return new TriggerKey(projectId, ScheduleGroup.ISSUE_SYNC.name());
    }
}

