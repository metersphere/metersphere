package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioReportService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/report")
public class ApiScenarioReportController {
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

}