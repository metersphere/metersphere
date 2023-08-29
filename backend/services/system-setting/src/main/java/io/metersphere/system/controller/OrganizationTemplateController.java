package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateUpdateRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.Template;
import io.metersphere.system.service.OrganizationTemplateLogService;
import io.metersphere.system.service.OrganizationTemplateService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-8-30
 */
@RestController
@RequestMapping("/organization/template")
@Tag(name = "系统设置-组织-模版")
public class OrganizationTemplateController {

    @Resource
    private OrganizationTemplateService organizationTemplateservice;

    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取模版列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String organizationId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,ISSUE,API,UI）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return organizationTemplateservice.list(organizationId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public TemplateDTO get(@PathVariable String id) {
        return organizationTemplateservice.geDTOWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#request)", msClass = OrganizationTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        template.setCreateUser(SessionUtils.getUserId());
        return organizationTemplateservice.add(template, request.getCustomFields());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = OrganizationTemplateLogService.class)
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        return organizationTemplateservice.update(template, request.getCustomFields());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = OrganizationTemplateLogService.class)
    public void delete(@PathVariable String id) {
        organizationTemplateservice.delete(id);
    }
}