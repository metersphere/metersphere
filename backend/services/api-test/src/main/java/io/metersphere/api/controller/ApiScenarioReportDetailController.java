package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioReportDetailService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/report/detail")
public class ApiScenarioReportDetailController {
    @Resource
    private ApiScenarioReportDetailService apiScenarioReportDetailService;

}