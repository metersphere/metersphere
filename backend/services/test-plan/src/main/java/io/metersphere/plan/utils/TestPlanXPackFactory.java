package io.metersphere.plan.utils;

import io.metersphere.plan.service.TestPlanGroupService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.service.LicenseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TestPlanXPackFactory {

    private TestPlanGroupService testPlanGroupService;
    private LicenseService licenseService;

    public TestPlanGroupService getTestPlanGroupService() {
        this.checkService();
        if (licenseValidate()) {
            return testPlanGroupService;
        } else {
            return null;
        }
    }

    private void checkService() {
        if (licenseService == null) {
            licenseService = CommonBeanFactory.getBean(LicenseService.class);
        }
        if (testPlanGroupService == null) {
            testPlanGroupService = CommonBeanFactory.getBean(TestPlanGroupService.class);
        }
    }

    private boolean licenseValidate() {
        LicenseDTO licenseDTO = licenseService.validate();
        return (licenseDTO != null && StringUtils.equals(licenseDTO.getStatus(), "valid"));
    }
}
