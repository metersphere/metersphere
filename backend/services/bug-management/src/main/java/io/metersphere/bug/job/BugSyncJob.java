package io.metersphere.bug.job;

import io.metersphere.bug.service.BugSyncExtraService;
import io.metersphere.bug.service.BugSyncService;
import io.metersphere.bug.service.XpackBugService;
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
    private final XpackBugService xpackBugService;
    private final BugSyncExtraService bugSyncExtraService;
    private final ProjectApplicationService projectApplicationService;

    public BugSyncJob() {
        licenseService = CommonBeanFactory.getBean(LicenseService.class);
        xpackBugService = CommonBeanFactory.getBean(XpackBugService.class);
        bugSyncService = CommonBeanFactory.getBean(BugSyncService.class);
        bugSyncExtraService = CommonBeanFactory.getBean(BugSyncExtraService.class);
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
        LogUtils.info("bug sync job start......");
        // 获取当前项目同步缺陷唯一Key
        try{
            String syncValue = bugSyncExtraService.getSyncKey(resourceId);
            if (StringUtils.isEmpty(syncValue)) {
                // 不存在, 设置保证唯一性, 并开始同步
                bugSyncExtraService.setSyncKey(resourceId);
                if (licenseService == null) {
                    LogUtils.info("license is null, sync remain bug");
                    bugSyncService.syncPlatformBugBySchedule(resourceId, userId);
                } else {
                    LicenseDTO licenseDTO = licenseService.validate();
                    if (licenseDTO != null && licenseDTO.getLicense() != null
                            && StringUtils.equals(licenseDTO.getStatus(), "valid")) {
                        LogUtils.info("license is valid, sync all bug");
                        xpackBugService.syncPlatformBugsBySchedule(resourceId, userId);
                    } else {
                        LogUtils.info("license is invalid, sync remain bug");
                        bugSyncService.syncPlatformBugBySchedule(resourceId, userId);
                    }
                }
                bugSyncExtraService.deleteSyncKey(resourceId);
            }
        } catch (Exception e) {
            bugSyncExtraService.deleteSyncKey(resourceId);
            LogUtils.error(e.getMessage());
        }
        LogUtils.info("bug sync job end......");
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
