package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.dto.request.StatusItemAddRequest;
import io.metersphere.sdk.dto.request.StatusItemUpdateRequest;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusFlowSettingDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.OrganizationStatusFlowSettingLogService;
import io.metersphere.system.service.OrganizationStatusFlowSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

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
    public StatusFlowSettingDTO getStatusFlowSetting(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                     @PathVariable String organizationId,
                                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                                     @PathVariable String scene) {
        return organizationStatusFlowSettingService.getStatusFlowSetting(organizationId, scene);
    }

    @PostMapping("/status/definition/update")
    @Operation(summary = "系统设置-组织-状态流设置-设置状态定义，即起始状态，结束状态")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusDefinitionLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    public void updateStatusDefinition(@RequestBody StatusDefinitionUpdateRequest request) {
        organizationStatusFlowSettingService.updateStatusDefinition(request);
    }

    @PostMapping("/status/add")
    @Operation(summary = "系统设置-组织-状态流设置-添加状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addStatusItemLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    public StatusItem addStatusItem(@RequestBody StatusItemAddRequest request) {
       return organizationStatusFlowSettingService.addStatusItem(request);
    }

    @PostMapping("/status/update")
    @Operation(summary = "系统设置-组织-状态流设置-修改状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusItemLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    public StatusItem updateStatusItem(@RequestBody StatusItemUpdateRequest request) {
        return organizationStatusFlowSettingService.updateStatusItem(request);
    }

    @GetMapping("/status/delete/{id}")
    @Operation(summary = "系统设置-组织-状态流设置-删除状态项")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteStatusItemLog(#id)", msClass = OrganizationStatusFlowSettingLogService.class)
    public void deleteStatusItem(@PathVariable String id) {
        organizationStatusFlowSettingService.deleteStatusItem(id);
    }

    @PostMapping("/status/flow/update")
    @Operation(summary = "系统设置-组织-状态流设置-设置状态流转")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusFlowLog(#request)", msClass = OrganizationStatusFlowSettingLogService.class)
    public void updateStatusFlow(@RequestBody StatusFlowUpdateRequest request) {
        organizationStatusFlowSettingService.updateStatusFlow(request);
    }
}