package io.metersphere.sdk.controller;

import io.metersphere.sdk.dto.LicenseDTO;
import io.metersphere.sdk.service.LicenseService;
import io.metersphere.sdk.util.CommonBeanFactory;
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
