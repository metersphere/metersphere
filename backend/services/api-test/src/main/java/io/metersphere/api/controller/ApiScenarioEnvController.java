package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioEnvService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/env")
public class ApiScenarioEnvController {
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;

}