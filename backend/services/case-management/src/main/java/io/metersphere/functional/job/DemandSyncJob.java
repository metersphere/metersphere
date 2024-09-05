package io.metersphere.functional.job;

import io.metersphere.functional.service.DemandSyncService;
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
 * 需求同步定时任务
 */
public class DemandSyncJob extends BaseScheduleJob {

    private final DemandSyncService demandSyncService;
    private final ProjectApplicationService projectApplicationService;

    public DemandSyncJob() {
        demandSyncService = CommonBeanFactory.getBean(DemandSyncService.class);
        projectApplicationService = CommonBeanFactory.getBean(ProjectApplicationService.class);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DemandSyncJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DemandSyncJob.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        String userId = jobDataMap.getString("userId");
        if (!checkBeforeSync(resourceId)) {
            return;
        }
        LogUtils.info("Start synchronizing demands");
        try{
            demandSyncService.syncPlatformDemandBySchedule(resourceId, userId);
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
        }
    }

    /**
     * 同步前检验, 同步配置的平台是否开启插件集成
     * @return 是否放行
     */
    private boolean checkBeforeSync(String projectId) {
        ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, false);
        return serviceIntegration != null && serviceIntegration.getEnable();
    }
}
