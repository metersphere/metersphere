package io.metersphere.api.controller;

import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.service.LicenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task/runner")
@Tag(name = "执行开放API")
public class ApiTaskRunnerController {

    @GetMapping("/get/{encrypt}")
    public String getCode(@PathVariable String encrypt) {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.getCode(encrypt);
        }
        return null;
    }
}
