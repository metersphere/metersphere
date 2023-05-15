package io.metersphere.api.controller;

import io.metersphere.api.service.ApiReportBlobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report/blob")
public class ApiReportBlobController {
    @Resource
    private ApiReportBlobService apiReportBlobService;

}