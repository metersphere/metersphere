package io.metersphere.bug.job;

import io.metersphere.bug.service.BugSyncService;
import io.metersphere.bug.service.XpackBugService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.service.LicenseService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

/**
 * 缺陷同步定时任务
 */
public class BugSyncJob extends BaseScheduleJob {

    private final LicenseService licenseService;
    private final BugSyncService bugSyncService;
    private final XpackBugService xpackBugService;

    public BugSyncJob() {
        licenseService = CommonBeanFactory.getBean(LicenseService.class);
        xpackBugService = CommonBeanFactory.getBean(XpackBugService.class);
        bugSyncService = CommonBeanFactory.getBean(BugSyncService.class);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LogUtils.info("bug sync job start......");
        if (licenseService == null) {
            LogUtils.info("license is null, sync remain bug");
            bugSyncService.syncPlatformBugBySchedule();
        } else {
            LicenseDTO licenseDTO = licenseService.validate();
            if (licenseDTO != null && licenseDTO.getLicense() != null
                    && StringUtils.equals(licenseDTO.getStatus(), "valid")) {
                LogUtils.info("license is valid, sync all bug");
                xpackBugService.syncPlatformBugsBySchedule();
            } else {
                LogUtils.info("license is invalid, sync remain bug");
                bugSyncService.syncPlatformBugBySchedule();
            }
        }
        LogUtils.info("bug sync job end......");
    }
}
