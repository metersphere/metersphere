package io.metersphere.job.schedule;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.sechedule.MsScheduleJob;
import io.metersphere.service.IssuesService;
import io.metersphere.xpack.license.dto.LicenseDTO;
import io.metersphere.xpack.license.service.LicenseService;
import io.metersphere.xpack.track.service.XpackIssueService;
import org.quartz.JobExecutionContext;

/**
 * @author songcc
 */
public class IssueSyncJob extends MsScheduleJob {

    private LicenseService licenseService;
    private XpackIssueService xpackIssueService;
    private IssuesService issuesService;

    public IssueSyncJob() {
        licenseService = CommonBeanFactory.getBean(LicenseService.class);
        xpackIssueService = CommonBeanFactory.getBean(XpackIssueService.class);
        issuesService = CommonBeanFactory.getBean(IssuesService.class);
    }

    @Override
    public void businessExecute(JobExecutionContext context) {
        LicenseDTO licenseDTO = licenseService.validate();
        if (licenseDTO != null && licenseDTO.getLicense() != null) {
            xpackIssueService.syncThirdPartyIssues();
        } else {
            issuesService.syncThirdPartyIssues();
        }
    }
}
