package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.request.testCaseReport.QueryTemplateRequest;
import io.metersphere.track.service.TestCaseReportTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/case/report/template")
@RestController
public class TestCaseReportTemplateController {

    @Resource
    TestCaseReportTemplateService testCaseReportTemplateService;

    @PostMapping("/list")
    public List<TestCaseReportTemplate> list(@RequestBody QueryTemplateRequest request) {
        return testCaseReportTemplateService.listTestCaseReportTemplate(request);
    }

    @GetMapping("/get/{id}")
    public TestCaseReportTemplate get(@PathVariable String id) {
        return testCaseReportTemplateService.getTestCaseReportTemplate(id);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS, type = OperLogConstants.CREATE, title = "#testCaseReportTemplate.name",sourceId = "#testCaseReportTemplate.id")
    public void add(@RequestBody TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateService.addTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS, type = OperLogConstants.UPDATE, title = "#testCaseReportTemplate.name",sourceId = "#testCaseReportTemplate.id")
    public void edit(@RequestBody TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateService.editTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestCaseReportTemplateService.class)
    public int delete(@PathVariable String id) {
        return testCaseReportTemplateService.deleteTestCaseReportTemplate(id);
    }

}
