package io.metersphere.project.controller;

import io.metersphere.project.dto.environment.EnvironmentRequest;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.ssl.KeyStoreEntry;
import io.metersphere.project.service.CommandService;
import io.metersphere.project.service.EnvironmentLogService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping(value = "/project/environment")
@Tag(name = "项目管理-环境")
public class EnvironmentController {

    @Resource
    private EnvironmentService environmentService;
    @Resource
    private CommandService commandService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "项目管理-环境-环境目录-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public List<Environment> list(@PathVariable String projectId) {
        return environmentService.list(projectId);
    }

    @GetMapping("/get/{environmentId}")
    @Operation(summary = "项目管理-环境-环境目录-详情")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public EnvironmentRequest get(@PathVariable String environmentId) {
        return environmentService.get(environmentId);
    }


    @PostMapping("/add")
    @Operation(summary = "项目管理-环境-环境目录-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = EnvironmentLogService.class)
    public EnvironmentRequest add(@Validated({Created.class}) @RequestPart(value = "request") EnvironmentRequest request,
                                  @RequestPart(value = "file", required = false) List<MultipartFile> sslFiles) {
        return environmentService.add(request, SessionUtils.getUserId(), sslFiles);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    @Operation(summary = "项目管理-环境-环境目录-修改")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = EnvironmentLogService.class)
    public EnvironmentRequest update(@Validated({Updated.class}) @RequestPart("request") EnvironmentRequest request,
                                     @RequestPart(value = "file", required = false) List<MultipartFile> sslFiles) {
        return environmentService.update(request, SessionUtils.getUserId(), sslFiles);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-环境-环境目录-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = EnvironmentLogService.class)
    public void delete(@PathVariable String id) {
        environmentService.delete(id);
    }

    @PostMapping("/database/validate")
    @Operation(summary = "项目管理-环境-数据库配置-校验")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ, PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE}, logical = Logical.OR)
    public void validate(@Validated @RequestBody DataSource databaseConfig) {
        environmentService.validateDataSource(databaseConfig);
    }

    @GetMapping("/database/driver-options/{organizationId}")
    @Operation(summary = "项目管理-环境-数据库配置-数据库驱动选项")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ, PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE}, logical = Logical.OR)
    public List<OptionDTO> driverOptions(@PathVariable String organizationId) {
        return environmentService.getDriverOptions(organizationId);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_IMPORT)
    @Operation(summary = "项目管理-环境-环境目录-导入")
    public void create(@RequestPart(value = "file") MultipartFile file) {
        environmentService.create(file, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/export")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_EXPORT)
    @Operation(summary = "项目管理-环境-环境目录-导出")
    public String export(@RequestBody List<String> environmentIds) {
        return environmentService.export(environmentIds);
    }

    @PostMapping(value = "/get/entry")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE}, logical = Logical.OR)
    public List<KeyStoreEntry> getEntry(@RequestPart("request") String password, @RequestPart(value = "file") MultipartFile sslFiles) {
        return commandService.getEntry(password, sslFiles);
    }

}
