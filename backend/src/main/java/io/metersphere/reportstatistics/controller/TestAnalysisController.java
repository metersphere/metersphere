package io.metersphere.reportstatistics.controller;

import io.metersphere.reportstatistics.dto.TestAnalysisChartRequest;
import io.metersphere.reportstatistics.dto.TestAnalysisResult;
import io.metersphere.reportstatistics.service.TestAnalysisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/report/test/analysis")
public class TestAnalysisController {

    @Resource
    TestAnalysisService testAnalysisService;

    @PostMapping("/getReport")
    public TestAnalysisResult getReport(@RequestBody TestAnalysisChartRequest request) {
        return testAnalysisService.getReport(request);
    }
}
