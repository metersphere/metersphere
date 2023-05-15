package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionMockConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/mock/config")
public class ApiDefinitionMockConfigController {
    @Resource
    private ApiDefinitionMockConfigService apiDefinitionMockConfigService;

}