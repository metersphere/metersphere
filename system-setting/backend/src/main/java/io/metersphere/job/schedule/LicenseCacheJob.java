package io.metersphere.job.schedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.xpack.license.dto.LicenseDTO;
import io.metersphere.xpack.license.service.LicenseService;
import org.springframework.stereotype.Component;

@Component
public class LicenseCacheJob {

    @QuartzScheduled(cron = "0 2 0 * * ?")
    public void checkLicenseTask() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            LicenseDTO dto = licenseService.refreshLicense();
            LogUtil.info("刷新LICENSE状态: " + dto.getStatus());
        }
    }
}
