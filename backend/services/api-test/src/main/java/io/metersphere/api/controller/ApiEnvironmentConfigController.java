package io.metersphere.api.controller;

import io.metersphere.api.service.ApiEnvironmentConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/environment/config")
public class ApiEnvironmentConfigController {
    @Resource
    private ApiEnvironmentConfigService apiEnvironmentConfigService;

}