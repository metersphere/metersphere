package io.metersphere.xpack.license.controller;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.xpack.license.dto.LicenseDTO;
import io.metersphere.xpack.license.service.LicenseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license")
public class LicenseValidateController {
    @GetMapping("/validate")
    public LicenseDTO validate() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.validate();
        }
        return new LicenseDTO();
    }
}
