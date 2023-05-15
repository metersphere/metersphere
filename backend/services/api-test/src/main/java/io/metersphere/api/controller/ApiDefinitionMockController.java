package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionMockService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/mock")
public class ApiDefinitionMockController {
    @Resource
    private ApiDefinitionMockService apiDefinitionMockService;

}