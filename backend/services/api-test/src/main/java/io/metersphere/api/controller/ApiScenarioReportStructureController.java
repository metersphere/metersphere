package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioReportStructureService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/report/structure")
public class ApiScenarioReportStructureController {
    @Resource
    private ApiScenarioReportStructureService apiScenarioReportStructureService;

}