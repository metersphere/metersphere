package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionTemplateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/template")
public class ApiDefinitionTemplateController {
    @Resource
    private ApiDefinitionTemplateService apiDefinitionTemplateService;

}