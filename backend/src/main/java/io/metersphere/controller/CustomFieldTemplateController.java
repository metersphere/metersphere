package io.metersphere.controller;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.dto.CustomFieldTemplateDao;
import io.metersphere.service.CustomFieldTemplateService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("custom/field/template")
public class CustomFieldTemplateController {

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public List<CustomFieldTemplateDao> list(@RequestBody CustomFieldTemplate request) {
        return customFieldTemplateService.list(request);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ_CUSTOM)
    public void update(@RequestBody CustomFieldTemplate request) {
        customFieldTemplateService.update(request);
    }

    @GetMapping("/{id}")
    public CustomField get(@PathVariable String id) {
        return customFieldTemplateService.getCustomField(id);
    }

}
