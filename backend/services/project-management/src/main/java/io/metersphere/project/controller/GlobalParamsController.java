package io.metersphere.project.controller;

import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.dto.environment.GlobalParamsRequest;
import io.metersphere.project.service.GlobalParamsLogService;
import io.metersphere.project.service.GlobalParamsService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.ProjectParameter;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project/global/params")
@Tag(name = "项目管理-环境-全局参数")
public class GlobalParamsController {

    @Resource
    private GlobalParamsService globalParamsService;

    @PostMapping("/add")
    @Operation(summary = "项目管理-环境-全局参数-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = GlobalParamsLogService.class)
    public ProjectParameter add(@Validated({Created.class}) @RequestBody GlobalParamsRequest request) {
        return globalParamsService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-环境-全局参数-修改")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = GlobalParamsLogService.class)
    @CheckOwner(resourceId = "#request.id", resourceType = "project_parameter")
    public ProjectParameter update(@Validated({Updated.class}) @RequestBody GlobalParamsRequest request) {
        return globalParamsService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/{projectId}")
    @Operation(summary = "项目管理-环境-全局参数-详情")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public GlobalParamsDTO get(@PathVariable String projectId) {
        return globalParamsService.get(projectId);
    }


    @GetMapping("/export/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_EXPORT)
    @Operation(summary = "项目管理-环境-全局参数-导出")
    public ResponseEntity<byte[]> export(@PathVariable String projectId) {
        return globalParamsService.exportJson(projectId);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_IMPORT)
    @Operation(summary = "项目管理-环境-全局参数-导入")
    public void create(@RequestPart(value = "file", required = false) MultipartFile file) {
        globalParamsService.importData(file, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

}
