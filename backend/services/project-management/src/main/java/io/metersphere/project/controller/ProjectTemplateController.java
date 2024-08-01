package io.metersphere.project.controller;

import io.metersphere.project.dto.ProjectTemplateDTO;
import io.metersphere.project.service.ProjectTemplateLogService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.Template;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckProjectOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author : jianxing
 * @date : 2023-8-30
 */
@RestController
@RequestMapping("/project/template")
@Tag(name = "项目管理-模版")
public class ProjectTemplateController {

    @Resource
    private ProjectTemplateService projectTemplateservice;
    @Resource
    private CommonFileService commonFileService;

    @GetMapping("/list/{projectId}/{scene}")
    @Operation(summary = "获取模版列表")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<ProjectTemplateDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String projectId,
                                         @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return projectTemplateservice.list(projectId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    @CheckProjectOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public TemplateDTO get(@PathVariable String id) {
        return projectTemplateservice.getTemplateDTOWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ProjectTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateservice.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectTemplateLogService.class)
    @CheckProjectOwner(resourceId = "#request.getId()", resourceType = "template", resourceCol = "scope_id")
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return projectTemplateservice.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ProjectTemplateLogService.class)
    @CheckProjectOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        projectTemplateservice.delete(id);
    }

    @GetMapping("/set-default/{projectId}/{id}")
    @Operation(summary = "设置默认模板")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.setDefaultTemplateLog(#id)", msClass = ProjectTemplateLogService.class)
    public void setDefaultTemplate(@PathVariable String projectId, @PathVariable String id) {
        projectTemplateservice.setDefaultTemplate(projectId, id);
    }

    @GetMapping("/enable/config/{projectId}")
    @Operation(summary = "是否启用组织模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public Map<String, Boolean> getProjectTemplateEnableConfig(@PathVariable String projectId) {
        return projectTemplateservice.getProjectTemplateEnableConfig(projectId);
    }

    @PostMapping("/upload/temp/img")
    @Operation(summary = "上传富文本图片，并返回文件ID")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TEMPLATE_UPDATE, PermissionConstants.PROJECT_TEMPLATE_ADD}, logical = Logical.OR)
    public String upload(@RequestParam("file") MultipartFile file) {
        return commonFileService.uploadTempImgFile(file);
    }

    @GetMapping(value = "/img/preview/{projectId}/{fileId}/{compressed}")
    @Operation(summary = "富文本图片-预览")
    public ResponseEntity<byte[]> previewImg(@PathVariable String projectId,
                                             @PathVariable String fileId,
                                             @Schema(description = "是否是压缩图片")
                                             @PathVariable("compressed") boolean compressed) {
        return projectTemplateservice.previewImg(projectId, fileId, compressed);
    }
}