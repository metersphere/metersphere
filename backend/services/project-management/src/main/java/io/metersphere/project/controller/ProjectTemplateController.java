package io.metersphere.project.controller;

import io.metersphere.project.service.ProjectTemplateLogService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.Template;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
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
@RequestMapping("/project/template")
@Tag(name = "系统设置-组织-模版")
public class ProjectTemplateController {

    @Resource
    private ProjectTemplateService projectTemplateservice;

    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取模版列表")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String projectId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,ISSUE,API,UI）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return projectTemplateservice.list(projectId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public TemplateDTO get(@PathVariable String id) {
        return projectTemplateservice.geDTOWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#request)", msClass = ProjectTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        template.setCreateUser(SessionUtils.getUserId());
        return projectTemplateservice.add(template, request.getCustomFields());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = ProjectTemplateLogService.class)
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        return projectTemplateservice.update(template, request.getCustomFields());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ProjectTemplateLogService.class)
    public void delete(@PathVariable String id) {
        projectTemplateservice.delete(id);
    }

    @GetMapping("/set-default/{id}")
    @Operation(summary = "设置模板模板")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.setDefaultTemplateLog(#id)", msClass = ProjectTemplateLogService.class)
    public void setDefaultTemplate(@PathVariable String id) {
        projectTemplateservice.setDefaultTemplate(id);
    }
}