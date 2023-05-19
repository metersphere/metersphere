package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioEnvironmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/env")
public class ApiScenarioEnvironmentController {
    @Resource
    private ApiScenarioEnvironmentService apiScenarioEnvironmentService;

}