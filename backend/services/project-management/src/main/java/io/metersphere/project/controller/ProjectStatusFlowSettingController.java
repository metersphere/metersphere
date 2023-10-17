package io.metersphere.project.controller;

import io.metersphere.project.service.ProjectStatusFlowSettingLogService;
import io.metersphere.project.service.ProjectStatusFlowSettingService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.request.StatusDefinitionUpdateRequest;
import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.dto.request.StatusItemAddRequest;
import io.metersphere.sdk.dto.request.StatusItemUpdateRequest;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.dto.StatusFlowSettingDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
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
@RequestMapping("/project/status/flow/setting")
@Tag(name = "项目管理-模板-状态流设置")
public class ProjectStatusFlowSettingController {

    @Resource
    private ProjectStatusFlowSettingService projectStatusFlowSettingService;

    @GetMapping("/get/{projectId}/{scene}")
    @Operation(summary = "项目管理-模板-状态流设置-获取状态流设置")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public StatusFlowSettingDTO getStatusFlowSetting(@Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                     @PathVariable String projectId,
                                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                                     @PathVariable String scene) {
        return projectStatusFlowSettingService.getStatusFlowSetting(projectId, scene);
    }

    @PostMapping("/status/definition/update")
    @Operation(summary = "项目管理-模板-状态流设置-设置状态定义，即起始状态，结束状态")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusDefinitionLog(#request)", msClass = ProjectStatusFlowSettingLogService.class)
    public void updateStatusDefinition(@RequestBody StatusDefinitionUpdateRequest request) {
        projectStatusFlowSettingService.updateStatusDefinition(request);
    }

    @PostMapping("/status/add")
    @Operation(summary = "项目管理-模板-状态流设置-添加状态项")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addStatusItemLog(#request)", msClass = ProjectStatusFlowSettingLogService.class)
    public StatusItem addStatusItem(@RequestBody StatusItemAddRequest request) {
       return projectStatusFlowSettingService.addStatusItem(request);
    }

    @PostMapping("/status/update")
    @Operation(summary = "项目管理-模板-状态流设置-修改状态项")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusItemLog(#request)", msClass = ProjectStatusFlowSettingLogService.class)
    public StatusItem addStatusItem(@RequestBody StatusItemUpdateRequest request) {
        return projectStatusFlowSettingService.updateStatusItem(request);
    }

    @GetMapping("/status/delete/{id}")
    @Operation(summary = "项目管理-模板-状态流设置-删除状态项")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteStatusItemLog(#id)", msClass = ProjectStatusFlowSettingLogService.class)
    public void deleteStatusItem(@PathVariable String id) {
        projectStatusFlowSettingService.deleteStatusItem(id);
    }

    @PostMapping("/status/flow/update")
    @Operation(summary = "项目管理-模板-状态流设置-设置状态流转")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateStatusFlowLog(#request)", msClass = ProjectStatusFlowSettingLogService.class)
    public void updateStatusFlow(@RequestBody StatusFlowUpdateRequest request) {
        projectStatusFlowSettingService.updateStatusFlow(request);
    }
}