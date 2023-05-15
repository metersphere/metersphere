package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionSwaggerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/swagger")
public class ApiDefinitionSwaggerController {
    @Resource
    private ApiDefinitionSwaggerService apiDefinitionSwaggerService;

}