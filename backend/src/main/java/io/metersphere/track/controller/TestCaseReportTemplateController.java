package io.metersphere.track.controller;

import io.metersphere.base.domain.TestCaseReportTemplate;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.track.request.testCaseReport.QueryTemplateRequest;
import io.metersphere.track.service.TestCaseReportTemplateService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void add(@RequestBody TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateService.addTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void edit(@RequestBody TestCaseReportTemplate testCaseReportTemplate) {
        testCaseReportTemplateService.editTestCaseReportTemplate(testCaseReportTemplate);
    }

    @PostMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int delete(@PathVariable String id) {
        return testCaseReportTemplateService.deleteTestCaseReportTemplate(id);
    }

}
