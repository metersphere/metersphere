package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTaskCenterService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: LAN
 * @date: 2024/1/17 19:19
 * @version: 1.0
 */
@RestController
@RequestMapping(value = "/task/center")
@Tag(name = "任务中心-实时任务-接口用例/场景")
public class ApiTaskCenterController {

    @Resource
    private ApiTaskCenterService apiTaskCenterService;

    private static final String PROJECT = "project";
    private static final String ORG = "org";
    private static final String SYSTEM = "system";


    @PostMapping("/api/project/real-time/page")
    @Operation(summary = "项目-任务中心-接口用例/场景-实时任务列表")
    public Pager<List<TaskCenterDTO>> projectList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getProjectPage(request, SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/api/org/real-time/page")
    @Operation(summary = "组织-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> orgList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getOrganizationPage(request, SessionUtils.getCurrentOrganizationId());
    }

    @PostMapping("/api/system/real-time/page")
    @Operation(summary = "系统-任务中心-接口用例/场景-实时任务列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TASK_CENTER_READ)
    public Pager<List<TaskCenterDTO>> systemList(@Validated @RequestBody TaskCenterPageRequest request) {
        return apiTaskCenterService.getSystemPage(request);
    }

    @PostMapping("/api/system/stop")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    public void systemStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        hasPermission(SYSTEM, request.getModuleType());
        apiTaskCenterService.systemStop(request, SessionUtils.getUserId());
    }

    @PostMapping("/api/org/stop")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void orgStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        hasPermission(ORG, request.getModuleType());
        apiTaskCenterService.orgStop(request, SessionUtils.getCurrentOrganizationId(), SessionUtils.getUserId());
    }

    @PostMapping("/api/project/stop")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void projectStop(@Validated @RequestBody TaskCenterBatchRequest request) {
        hasPermission(PROJECT, request.getModuleType());
        apiTaskCenterService.projectStop(request, SessionUtils.getCurrentProjectId(), SessionUtils.getUserId());
    }

    @GetMapping("/api/project/stop/{moduleType}/{id}")
    @Operation(summary = "项目-任务中心-接口用例/场景-停止任务")
    public void stopById(@PathVariable String moduleType, @PathVariable String id) {
        hasPermission(PROJECT, moduleType);
        apiTaskCenterService.stopById(moduleType, id, SessionUtils.getUserId(), OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, "/task/center/api/project/stop");
    }

    @GetMapping("/api/org/stop/{moduleType}/{id}")
    @Operation(summary = "组织-任务中心-接口用例/场景-停止任务")
    public void stopOrgById(@PathVariable String moduleType, @PathVariable String id) {
        hasPermission(ORG, moduleType);
        apiTaskCenterService.stopById(moduleType, id, SessionUtils.getUserId(), OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER, "/task/center/api/org/stop");
    }

    @GetMapping("/api/system/stop/{moduleType}/{id}")
    @Operation(summary = "系统-任务中心-接口用例/场景-停止任务")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_READ)
    public void stopSystemById(@PathVariable String moduleType,@PathVariable String id) {
        hasPermission(SYSTEM, moduleType);
        apiTaskCenterService.stopById(moduleType ,id, SessionUtils.getUserId(), OperationLogModule.SETTING_SYSTEM_TASK_CENTER, "/task/center/api/system/stop");
    }

    private void hasPermission(String type, String moduleType) {
        Map<String, List<String>> orgPermission = new HashMap<>(2);
        orgPermission.put(TaskCenterResourceType.API_CASE.toString(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE));
        orgPermission.put(TaskCenterResourceType.API_SCENARIO.toString(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE));
        Map<String, List<String>> projectPermission = new HashMap<>(2);
        projectPermission.put(TaskCenterResourceType.API_CASE.toString(), List.of(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE));
        projectPermission.put(TaskCenterResourceType.API_SCENARIO.toString(), List.of(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE));
        Map<String, List<String>> systemPermission = new HashMap<>(2);
        systemPermission.put(TaskCenterResourceType.API_CASE.toString(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE));
        systemPermission.put(TaskCenterResourceType.API_SCENARIO.toString(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE));

        boolean hasPermission = switch (type) {
            case ORG ->
                    orgPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId(), item));
            case PROJECT ->
                    projectPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId(), item));
            case SYSTEM ->
                    systemPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(SessionUtils.getCurrentOrganizationId(), SessionUtils.getCurrentProjectId(), item));
            default -> false;
        };
        if (!hasPermission) {
            throw new MSException(Translator.get("no_permission_to_resource"));
        }
    }


}
