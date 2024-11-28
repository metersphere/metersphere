package io.metersphere.bug.job;

import io.metersphere.bug.service.BugSyncService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.schedule.BaseScheduleJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * 缺陷同步定时任务
 * @author song-cc-rock
 */
public class BugSyncJob extends BaseScheduleJob {

    private final BugSyncService bugSyncService;
    private final ProjectApplicationService projectApplicationService;

    public BugSyncJob() {
        bugSyncService = CommonBeanFactory.getBean(BugSyncService.class);
        projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, BugSyncJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, BugSyncJob.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        String userId = jobDataMap.getString("userId");
        if (!checkBeforeSync(resourceId)) {
            return;
        }
        LogUtils.info("Start synchronization defect");
        try{
            // 根据项目默认配置同步缺陷
            boolean increment = projectApplicationService.isPlatformSyncMethodByIncrement(resourceId);
            if (increment) {
                // 增量同步
                LogUtils.info("Incremental Synchronization");
                bugSyncService.syncPlatformBugBySchedule(resourceId, userId);
            } else {
                // 全量同步
                LogUtils.info("Full Synchronization");
                bugSyncService.syncPlatformAllBugBySchedule(resourceId, userId);
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
        }
    }

    /**
     * 同步前检验, 同步配置的平台是否开启插件集成
     * @return 是否放行
     */
    private boolean checkBeforeSync(String projectId) {
        ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
        return serviceIntegration != null && serviceIntegration.getEnable();
    }
}
