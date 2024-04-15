package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusItemDTO;
import io.metersphere.system.dto.sdk.request.StatusDefinitionUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusFlowUpdateRequest;
import io.metersphere.system.dto.sdk.request.StatusItemAddRequest;
import io.metersphere.system.dto.sdk.request.StatusItemUpdateRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOrgOwner;
import io.metersphere.system.service.OrganizationStatusFlowSettingLogService;
import io.metersphere.system.service.OrganizationStatusFlowSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@RestController
@RequestMapping("/organization/status/flow/setting")
@Tag(name = "系统设置-组织-状态流设置")
public class OrganizationStatusFlowSettingController {

    @Resource
    private OrganizationStatusFlowSettingService organizationStatusFlowSettingService;

    @GetMapping("/get/{organizationId}/{scene}")
    @Operation(summary = "系统设置-组织-状态流设置-获取状态流设置")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public List<StatusItemDTO> getStatusFlowSetting(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                    @PathVariable String organizationId,
                                                    @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                                    @PathVariable String scene) {
        return organizationStatusFlowSettingService.getStatusFlowSetting(organizationId, scene);
    }

    @PostMapping("/status/definition/update")
    @Operation(summary = "系统设置-组织-状态流设置-设置状态定义，即起始状态，结束状态")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusDefinitionLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    @CheckOrgOwner(resourceId = "#request.getStatusId()", resourceType = "status_item", resourceCol = "scope_id")
    public void updateStatusDefinition(@Validated @RequestBody StatusDefinitionUpdateRequest request) {
        organizationStatusFlowSettingService.updateStatusDefinition(request);
    }

    @PostMapping("/status/add")
    @Operation(summary = "系统设置-组织-状态流设置-添加状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addStatusItemLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    public StatusItem addStatusItem(@Validated @RequestBody StatusItemAddRequest request) {
        return organizationStatusFlowSettingService.addStatusItem(request);
    }

    @PostMapping("/status/update")
    @Operation(summary = "系统设置-组织-状态流设置-修改状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusItemLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    @CheckOrgOwner(resourceId = "#request.getId()", resourceType = "status_item", resourceCol = "scope_id")
    public StatusItem updateStatusItem(@Validated @RequestBody StatusItemUpdateRequest request) {
        return organizationStatusFlowSettingService.updateStatusItem(request);
    }

    @PostMapping("/status/sort/{organizationId}/{scene}")
    @Operation(summary = "系统设置-组织-状态流设置-状态项排序")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @CheckOrgOwner(resourceId = "#statusIds", resourceType = "status_item", resourceCol = "scope_id")
    public void sortStatusItem(@PathVariable
                               String organizationId, @PathVariable String scene,
                               @RequestBody
                               @NotEmpty
                               List<String> statusIds) {
        organizationStatusFlowSettingService.sortStatusItem(organizationId, scene, statusIds);
    }

    @GetMapping("/status/delete/{id}")
    @Operation(summary = "系统设置-组织-状态流设置-删除状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteStatusItemLog(#id)", msClass = OrganizationStatusFlowSettingLogService.class)
    @CheckOrgOwner(resourceId = "#id", resourceType = "status_item", resourceCol = "scope_id")
    public void deleteStatusItem(@PathVariable String id) {
        organizationStatusFlowSettingService.deleteStatusItem(id);
    }

    @PostMapping("/status/flow/update")
    @Operation(summary = "系统设置-组织-状态流设置-设置状态流转")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusFlowLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    @CheckOrgOwner(resourceId = "#request.getFromId()", resourceType = "status_item", resourceCol = "scope_id")
    public void updateStatusFlow(@Validated @RequestBody StatusFlowUpdateRequest request) {
        organizationStatusFlowSettingService.updateStatusFlow(request);
    }
}