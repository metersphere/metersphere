package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioModuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/module")
public class ApiScenarioModuleController {
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;

}