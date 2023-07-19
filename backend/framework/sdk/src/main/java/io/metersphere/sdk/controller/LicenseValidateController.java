package io.metersphere.sdk.controller;

import io.metersphere.sdk.dto.LicenseDTO;
import io.metersphere.sdk.service.LicenseService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license")
@Tag(name="license校验")
public class LicenseValidateController {
    @GetMapping("/validate")
    @Operation(summary = "license校验")
    public LicenseDTO validate() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.validate();
        }
        return new LicenseDTO();
    }
}
