package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseTemplate;
import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.TestCaseTemplateDao;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.UpdateCaseFieldTemplateRequest;
import io.metersphere.service.TestCaseTemplateService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/field/template/case")
@RestController

public class TestCaseTemplateController {

    @Resource
    private TestCaseTemplateService testCaseTemplateService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseTemplateService.class)
    public void add(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseTemplateService.add(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE)
    public Pager<List<TestCaseTemplateWithBLOBs>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<List<TestCaseTemplateWithBLOBs>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseTemplateService.list(request));
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestCaseTemplateService.class)
    public void delete(@PathVariable(value = "id") String id) {
        testCaseTemplateService.delete(id);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseTemplateService.class)
    public void update(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseTemplateService.update(request);
    }

    @GetMapping({"/option/{projectId}", "/option"})
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE,
            PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_CREATE, PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_EDIT}, logical = Logical.OR)
    public List<TestCaseTemplate> list(@PathVariable(required = false) String projectId) {
        return testCaseTemplateService.getOption(projectId);
    }

    @GetMapping("/get/relate/{projectId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE, PermissionConstants.PROJECT_TRACK_CASE_READ}, logical = Logical.OR)
    public TestCaseTemplateDao getTemplate(@PathVariable String projectId) {
        return testCaseTemplateService.getTemplate(projectId);
    }
}
