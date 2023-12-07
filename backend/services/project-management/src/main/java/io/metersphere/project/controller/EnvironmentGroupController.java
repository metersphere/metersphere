package io.metersphere.project.controller;


import io.metersphere.project.dto.environment.EnvironmentFilterRequest;
import io.metersphere.project.dto.environment.EnvironmentGroupInfo;
import io.metersphere.project.dto.environment.EnvironmentGroupRequest;
import io.metersphere.project.service.EnvironmentGroupLogService;
import io.metersphere.project.service.EnvironmentGroupService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/project/environment/group")
@Tag(name = "项目管理-环境组")
public class EnvironmentGroupController {

    @Resource
    private EnvironmentGroupService environmentGroupService;

    @PostMapping("/add")
    @Operation(summary = "项目管理-环境组-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = EnvironmentGroupLogService.class)
    public EnvironmentGroup add(@Validated @RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.add(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-环境组-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = EnvironmentGroupLogService.class)
    public void delete(@PathVariable String id) {
        environmentGroupService.delete(id);
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-环境组-修改")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = EnvironmentGroupLogService.class)
    public EnvironmentGroup update(@Validated @RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.update(request, SessionUtils.getUserId());
    }

    @PostMapping("/list")
    @Operation(summary = "项目管理-环境组-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public List<EnvironmentGroup> list(@RequestBody EnvironmentFilterRequest request) {
        return environmentGroupService.list(request);
    }

    @GetMapping("/get/{groupId}")
    @Operation(summary = "项目管理-环境组-详情")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public List<EnvironmentGroupInfo> get(@PathVariable String groupId) {
        return environmentGroupService.get(groupId);
    }

    @GetMapping("/get-project")
    @Operation(summary = "项目管理-环境组-获取项目")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public List<OptionDTO> getProject() {
        return environmentGroupService.getProject(SessionUtils.getUserId());
    }
}
