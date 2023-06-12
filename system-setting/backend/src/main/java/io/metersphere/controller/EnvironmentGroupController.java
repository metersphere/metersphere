package io.metersphere.controller;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.environment.dto.EnvironmentGroupRequest;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.EnvironmentGroupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(value = "/environment/group")
public class EnvironmentGroupController {

    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    @PostMapping("/add")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_GROUP_READ)
    public EnvironmentGroup add(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.add(request);
    }

    @PostMapping("/batch/add")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_READ_CREATE)
    public void batchAdd(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.batchAdd(request);
    }

    @GetMapping("/delete/{id}")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_GROUP_DELETE)
    public void delete(@PathVariable String id) {
        environmentGroupService.delete(id);
    }

    @PostMapping("/update")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_GROUP_EDIT)
    public EnvironmentGroup update(@RequestBody EnvironmentGroupRequest request) {
        return environmentGroupService.update(request);
    }

    @PostMapping("/modify")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_GROUP_EDIT)
    public void modify(@RequestBody EnvironmentGroupRequest request) {
        environmentGroupService.modify(request);
    }

    @GetMapping("/copy/{id}")
    @MsRequestLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_GROUP_COPY)
    public void copy(@PathVariable String id) {
        environmentGroupService.copy(id);
    }

    @GetMapping("/project/map/name/{groupId}")
    public Map<String, String> getEnvNameMap(@PathVariable String groupId) {
        return environmentGroupProjectService.getEnvNameMap(groupId);
    }
}
