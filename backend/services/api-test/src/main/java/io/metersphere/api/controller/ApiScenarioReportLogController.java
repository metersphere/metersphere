package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioReportLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/report/log")
public class ApiScenarioReportLogController {
    @Resource
    private ApiScenarioReportLogService apiScenarioReportLogService;

}