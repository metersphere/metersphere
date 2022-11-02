package io.metersphere.xpack.utils;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.xpack.license.service.LicenseService;

public class XpackUtil {

    public static final String LICENSE_STATUS = "valid";

    public static boolean validateLicense() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.validate().getStatus().equals(LICENSE_STATUS);
        }
        return false;
    }
}
