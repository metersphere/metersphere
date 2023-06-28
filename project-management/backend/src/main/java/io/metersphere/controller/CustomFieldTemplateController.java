package io.metersphere.controller;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldTemplateDao;
import io.metersphere.service.CustomFieldTemplateService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("custom/field/template")
public class CustomFieldTemplateController {

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @PostMapping("/list")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM, PermissionConstants.PROJECT_TEMPLATE_READ_CASE_TEMPLATE,
            PermissionConstants.PROJECT_TEMPLATE_READ_ISSUE_TEMPLATE, PermissionConstants.PROJECT_TEMPLATE_READ_API_TEMPLATE}, logical = Logical.OR)
    public List<CustomFieldTemplateDao> list(@RequestBody CustomFieldTemplate request) {
        return customFieldTemplateService.list(request);
    }

    @GetMapping("/{id}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM,
            PermissionConstants.PROJECT_TRACK_ISSUE_READ, PermissionConstants.PROJECT_TRACK_CASE_READ, PermissionConstants.PROJECT_API_DEFINITION_READ}, logical = Logical.OR)
    public CustomField get(@PathVariable String id) {
        return customFieldTemplateService.getCustomField(id);
    }

    @GetMapping("/list/{templateId}")
    public List<CustomFieldDao> getCustomFieldByTemplateId(@PathVariable String templateId) {
        return customFieldTemplateService.getCustomFieldByTemplateId(templateId);
    }
}
