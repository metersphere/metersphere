package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioReferenceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/reference")
public class ApiScenarioReferenceController {
    @Resource
    private ApiScenarioReferenceService apiScenarioReferenceService;

}