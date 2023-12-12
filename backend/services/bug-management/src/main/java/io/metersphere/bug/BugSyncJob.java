package io.metersphere.bug;

import io.metersphere.bug.service.BugSyncService;
import io.metersphere.bug.service.XpackBugService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.sechedule.BaseScheduleJob;
import io.metersphere.system.service.LicenseService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

/**
 * 缺陷同步定时任务
 */
public class BugSyncJob extends BaseScheduleJob {

    private final LicenseService licenseService;

    private final XpackBugService xpackBugService;

    private final BugSyncService bugSyncService;

    public BugSyncJob() {
        licenseService = CommonBeanFactory.getBean(LicenseService.class);
        xpackBugService = CommonBeanFactory.getBean(XpackBugService.class);
        bugSyncService = CommonBeanFactory.getBean(BugSyncService.class);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LicenseDTO licenseDTO = licenseService.validate();
        if (licenseDTO != null && licenseDTO.getLicense() != null
                && StringUtils.equals(licenseDTO.getStatus(), "valid")) {
            LogUtils.info("sync all bug");
            xpackBugService.syncPlatformBugsBySchedule();
        } else {
            LogUtils.info("sync remain bug");
            bugSyncService.syncPlatformBugBySchedule();
        }
        LogUtils.info("sync bug end");
    }
}
