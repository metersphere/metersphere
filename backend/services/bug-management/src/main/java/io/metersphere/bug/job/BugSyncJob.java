package io.metersphere.bug.job;

import io.metersphere.bug.service.BugSyncService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.service.LicenseService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * 缺陷同步定时任务
 */
public class BugSyncJob extends BaseScheduleJob {

    private final LicenseService licenseService;
    private final BugSyncService bugSyncService;
    private final ProjectApplicationService projectApplicationService;

    public BugSyncJob() {
        licenseService = CommonBeanFactory.getBean(LicenseService.class);
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
        LogUtils.info("Start verify license and synchronizing bugs");
        try{
            if (licenseService == null) {
                // 没有License, 同步存量缺陷
                LogUtils.info("No license, synchronization stock bug");
                bugSyncService.syncPlatformBugBySchedule(resourceId, userId);
            } else {
                LicenseDTO licenseDTO = licenseService.validate();
                if (licenseDTO != null && licenseDTO.getLicense() != null
                        && StringUtils.equals(licenseDTO.getStatus(), "valid")) {
                    // License校验成功, 同步全量缺陷
                    LogUtils.info("License verification successful, synchronization full bug");
                    bugSyncService.syncPlatformAllBugBySchedule(resourceId, userId);
                } else {
                    // License校验失败, 同步存量缺陷
                    LogUtils.info("License verification failed, synchronization stock bug");
                    bugSyncService.syncPlatformBugBySchedule(resourceId, userId);
                }
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
