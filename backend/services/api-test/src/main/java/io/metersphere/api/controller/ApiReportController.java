package io.metersphere.api.controller;

import io.metersphere.api.service.ApiReportService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
public class ApiReportController {
    @Resource
    private ApiReportService apiReportService;

}