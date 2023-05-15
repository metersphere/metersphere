package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionModuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/module")
public class ApiDefinitionModuleController {
    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;

}