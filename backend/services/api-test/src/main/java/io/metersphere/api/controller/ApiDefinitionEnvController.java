package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionEnvService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/env")
public class ApiDefinitionEnvController {
    @Resource
    private ApiDefinitionEnvService apiDefinitionEnvService;

}