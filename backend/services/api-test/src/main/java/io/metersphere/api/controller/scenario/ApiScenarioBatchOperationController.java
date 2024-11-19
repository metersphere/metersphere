package io.metersphere.api.controller.scenario;

import io.metersphere.api.constants.ApiResource;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.service.ApiValidateService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.api.service.scenario.ApiScenarioNoticeService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/scenario")
@Tag(name = "接口测试-接口场景批量操作")
public class ApiScenarioBatchOperationController {
    @Resource
    private ApiValidateService apiValidateService;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;

    @PostMapping("/batch-operation/edit")
    @Operation(summary = "接口测试-接口场景批量操作-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchUpdate(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        apiScenarioService.batchEdit(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-operation/delete-gc")
    @Operation(summary = "接口测试-接口场景批量操作-回收站列表-批量删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
   public ApiScenarioBatchOperationResponse deleteFromGc(@Validated @RequestBody ApiScenarioBatchRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchGCOperation(request, true, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/delete-gc", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch-operation/recover-gc")
    @Operation(summary = "接口测试-接口场景批量操作-回收站列表-批量恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiScenarioBatchOperationResponse recoverFromGc(@Validated @RequestBody ApiScenarioBatchRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchGCOperation(request, false, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/recover-gc", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch-operation/delete")
    @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiScenarioBatchOperationResponse batchDelete(@Validated @RequestBody ApiScenarioBatchRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchDelete(request, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/delete", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch-operation/move")
    @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量移动")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getBatchOptionScenarios(#request)", targetClass = ApiScenarioNoticeService.class)
    public ApiScenarioBatchOperationResponse batchMove(@Validated @RequestBody ApiScenarioBatchCopyMoveRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchMove(request, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/move", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch-operation/copy")
    @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量复制")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiScenarioBatchOperationResponse batchCopy(@Validated @RequestBody ApiScenarioBatchCopyMoveRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchCopy(request, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/copy", HttpMethodConstants.POST.name()));
    }

    @PostMapping("/batch-operation/run")
    @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量执行")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    public void batchRun(@Validated @RequestBody ApiScenarioBatchRunRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        apiScenarioBatchRunService.batchRun(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-operation/schedule-config")
    @Operation(summary = "接口测试-接口场景管理-定时任务批量配置")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void scheduleConfig(@Validated @RequestBody ApiScenarioBatchScheduleConfigRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        apiScenarioService.batchScheduleConfig(request, SessionUtils.getUserId());
    }
}
