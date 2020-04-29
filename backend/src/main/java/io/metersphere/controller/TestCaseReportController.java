package io.metersphere.controller;

import io.metersphere.base.domain.TestCaseReport;
import io.metersphere.service.TestCaseReportService;
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
    public TestCaseReport get(@PathVariable Long id){
        return testCaseReportService.getTestCaseReport(id);
    }

    @PostMapping("/add")
    public void add(@RequestBody TestCaseReport TestCaseReport){
        testCaseReportService.addTestCaseReport(TestCaseReport);
    }

    @PostMapping("/edit")
    public void edit(@RequestBody TestCaseReport TestCaseReport){
        testCaseReportService.editTestCaseReport(TestCaseReport);
    }

    @PostMapping("/delete/{id}")
    public int delete(@PathVariable Long id){
        return testCaseReportService.deleteTestCaseReport(id);
    }

}
