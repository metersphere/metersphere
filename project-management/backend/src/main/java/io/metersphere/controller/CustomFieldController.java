package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.CustomField;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.QueryCustomFieldRequest;
import io.metersphere.service.CustomFieldService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("custom/field")
@RestController

public class CustomFieldController {

    @Resource
    private CustomFieldService customFieldService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_FIELD, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#customField.id)", msClass = CustomFieldService.class)
    public String add(@RequestBody CustomField customField) {
        return customFieldService.add(customField);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public Pager<List<CustomField>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCustomFieldRequest request) {
        Page<List<CustomField>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, customFieldService.list(request));
    }

    @PostMapping("/list/relate/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public Pager<List<CustomField>> listRelate(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCustomFieldRequest request) {
        return customFieldService.listRelate(goPage, pageSize, request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_FIELD, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = CustomFieldService.class)
    public void delete(@PathVariable(value = "id") String id) {
        customFieldService.delete(id);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM,
            PermissionConstants.PROJECT_TRACK_ISSUE_READ, PermissionConstants.PROJECT_TRACK_CASE_READ, PermissionConstants.PROJECT_API_DEFINITION_READ}, logical = Logical.OR)
    public CustomField get(@PathVariable(value = "id") String id) {
        return customFieldService.get(id);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_FIELD, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#customField.id)", content = "#msClass.getLogDetails(#customField.id)", msClass = CustomFieldService.class)
    public void update(@RequestBody CustomField customField) {
        customFieldService.update(customField);
    }

    @PostMapping("/list/ids")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public List<String> list(@RequestBody QueryCustomFieldRequest request) {
        return customFieldService.listIds(request);
    }

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public List<CustomField> getList(@RequestBody QueryCustomFieldRequest request) {
        return customFieldService.list(request);
    }

    @PostMapping("/default")
    public List<CustomField> getDefaultList(@RequestBody QueryCustomFieldRequest request) {
        return customFieldService.getDefaultField(request);
    }
}
