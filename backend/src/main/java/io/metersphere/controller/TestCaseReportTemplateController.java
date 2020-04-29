package io.metersphere.controller;

import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.service.TestCaseReportTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/case/report/template")
@RestController
public class TestCaseReportTemplateController {

    @Resource
    TestCaseReportTemplateService testCaseReportTemplateService;

    @PostMapping("/list")
    public List<TestCaseReportTemplate> list(@RequestBody TestCaseReportTemplate request) {
        return testCaseReportTemplateService.listTestCaseReportTemplate(request);
    }

    @GetMapping("/get/{id}")
    public TestCaseReportTemplate get(@PathVariable Long id){
        return testCaseReportTemplateService.getTestCaseReportTemplate(id);
    }

    @PostMapping("/add")
    public void add(@RequestBody TestCaseReportTemplate testCaseReportTemplate){
        testCaseReportTemplateService.addTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/edit")
    public void edit(@RequestBody TestCaseReportTemplate testCaseReportTemplate){
        testCaseReportTemplateService.editTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/delete/{id}")
    public int delete(@PathVariable Long id){
        return testCaseReportTemplateService.deleteTestCaseReportTemplate(id);
    }

}
