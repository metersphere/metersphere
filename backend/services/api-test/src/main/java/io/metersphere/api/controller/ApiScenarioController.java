package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario")
public class ApiScenarioController {
    @Resource
    private ApiScenarioService apiScenarioService;

}