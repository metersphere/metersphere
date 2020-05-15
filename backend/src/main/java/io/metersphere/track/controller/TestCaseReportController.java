package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.track.request.testCaseReport.CreateReportRequest;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.service.TestCaseReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/case/report")
@RestController
public class TestCaseReportController {

    @Resource
    TestCaseReportService testCaseReportService;

    @PostMapping("/list")
    public List<TestCaseReport> list(@RequestBody TestCaseReport request) {
        return testCaseReportService.listTestCaseReport(request);
    }

    @GetMapping("/get/{id}")
    public TestCaseReport get(@PathVariable String id){
        return testCaseReportService.getTestCaseReport(id);
    }

    @PostMapping("/add")
    public String addByTemplateId(@RequestBody CreateReportRequest request){
        return testCaseReportService.addTestCaseReportByTemplateId(request);
    }

    @PostMapping("/edit")
    public void edit(@RequestBody TestCaseReport TestCaseReport){
        testCaseReportService.editTestCaseReport(TestCaseReport);
    }

    @PostMapping("/delete/{id}")
    public int delete(@PathVariable String id){
        return testCaseReportService.deleteTestCaseReport(id);
    }

}
