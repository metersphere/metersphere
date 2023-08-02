package io.metersphere.system.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.sdk.dto.LicenseDTO;
import io.metersphere.sdk.service.LicenseService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class LicenseCacheJob {

    @QuartzScheduled(cron = "0 5 0 * * ?")
    public void checkLicenseTask() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            LicenseDTO dto = licenseService.refreshLicense();
            LogUtils.info("刷新LICENSE状态: " + dto.getStatus());
        }
    }
}
