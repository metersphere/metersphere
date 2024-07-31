package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.Template;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOrgOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.service.OrganizationTemplateLogService;
import io.metersphere.system.service.OrganizationTemplateService;
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
@RequestMapping("/organization/template")
@Tag(name = "系统设置-组织-模版")
public class OrganizationTemplateController {

    @Resource
    private OrganizationTemplateService organizationTemplateService;
    @Resource
    private CommonFileService commonFileService;

    @GetMapping("/list/{organizationId}/{scene}")
    @Operation(summary = "获取模版列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String organizationId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return organizationTemplateService.list(organizationId, scene);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取模版详情")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    @CheckOrgOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public TemplateDTO get(@PathVariable String id) {
        return organizationTemplateService.geDTOWithCheck(id);
    }

    @PostMapping("/add")
    @Operation(summary = "创建模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#request)", msClass = OrganizationTemplateLogService.class)
    public Template add(@Validated({Created.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = OrganizationTemplateLogService.class)
    @CheckOrgOwner(resourceId = "#request.getId()", resourceType = "template", resourceCol = "scope_id")
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = OrganizationTemplateLogService.class)
    @CheckOrgOwner(resourceId = "#id", resourceType = "template", resourceCol = "scope_id")
    public void delete(@PathVariable String id) {
        organizationTemplateService.delete(id);
    }

    @GetMapping("/disable/{organizationId}/{scene}")
    @Operation(summary = "关闭组织模板，开启项目模板")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ENABLE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.disableOrganizationTemplateLog(#organizationId,#scene)", msClass = OrganizationTemplateLogService.class)
    public void disableOrganizationTemplate(@PathVariable String organizationId, @PathVariable String scene) {
        organizationTemplateService.disableOrganizationTemplate(organizationId, scene);
    }

    @GetMapping("/enable/config/{organizationId}")
    @Operation(summary = "是否启用组织模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(@PathVariable String organizationId) {
        return organizationTemplateService.getOrganizationTemplateEnableConfig(organizationId);
    }

    @PostMapping("/upload/temp/img")
    @Operation(summary = "上传富文本图片，并返回文件ID")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE, PermissionConstants.ORGANIZATION_TEMPLATE_ADD}, logical = Logical.OR)
    public String upload(@RequestParam("file") MultipartFile file) {
        return commonFileService.uploadTempImgFile(file);
    }

    @GetMapping(value = "/img/preview/{organizationId}/{fileId}/{compressed}")
    @Operation(summary = "富文本图片-预览")
    public ResponseEntity<byte[]> previewImg(@PathVariable String organizationId,
                                                  @PathVariable String fileId,
                                                  @Schema(description = "查看压缩图片", requiredMode = Schema.RequiredMode.REQUIRED)
                                                  @PathVariable("compressed") boolean compressed) {
        return organizationTemplateService.previewImg(organizationId, fileId, compressed);
    }
}