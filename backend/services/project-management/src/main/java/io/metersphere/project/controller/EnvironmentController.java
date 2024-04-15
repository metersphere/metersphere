package io.metersphere.project.controller;

import io.metersphere.project.dto.environment.*;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.ssl.KeyStoreEntry;
import io.metersphere.project.service.CommandService;
import io.metersphere.project.service.EnvironmentLogService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/list")
    @Operation(summary = "项目管理-环境-环境目录-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<Environment> list(@Validated @RequestBody EnvironmentFilterRequest request) {
        return environmentService.list(request);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "项目管理-环境-环境目录-详情")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    @CheckOwner(resourceId = "#id", resourceType = "environment")
    public EnvironmentInfoDTO get(@PathVariable String id) {
        return environmentService.get(id);
    }

    @GetMapping("/scripts/{projectId}")
    @Operation(summary = "项目管理-环境-环境目录-接口插件前端配置脚本列表")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<EnvironmentPluginScriptDTO> getPluginScripts(@PathVariable String projectId) {
        return environmentService.getPluginScripts(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-环境-环境目录-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = EnvironmentLogService.class)
    public Environment add(@Validated({Created.class}) @RequestPart(value = "request") EnvironmentRequest request,
                           @RequestPart(value = "file", required = false) List<MultipartFile> sslFiles) {
        return environmentService.add(request, SessionUtils.getUserId(), sslFiles);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    @Operation(summary = "项目管理-环境-环境目录-修改")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = EnvironmentLogService.class)
    @CheckOwner(resourceId = "#request.id", resourceType = "environment")
    public Environment update(@Validated({Updated.class}) @RequestPart("request") EnvironmentRequest request,
                              @RequestPart(value = "file", required = false) List<MultipartFile> sslFiles) {
        return environmentService.update(request, SessionUtils.getUserId(), sslFiles);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "项目管理-环境-环境目录-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "environment")
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
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<OptionDTO> driverOptions(@PathVariable String organizationId) {
        return environmentService.getDriverOptions(organizationId);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_IMPORT)
    @Operation(summary = "项目管理-环境-环境目录-导入")
    public void create(@RequestPart(value = "request") EnvironmentImportRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        environmentService.create(request, file, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/export")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_EXPORT)
    @Operation(summary = "项目管理-环境-环境目录-导出")
    public ResponseEntity<byte[]> export(@Validated @RequestBody TableBatchProcessDTO request) {
        return environmentService.exportJson(request, SessionUtils.getCurrentProjectId());
    }

    @PostMapping(value = "/get/entry")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE}, logical = Logical.OR)
    public List<KeyStoreEntry> getEntry(@RequestPart("request") String password, @RequestPart(value = "file") MultipartFile sslFiles) {
        return commandService.getEntry(password, sslFiles);
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "项目管理-环境-环境目录-修改排序")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    public void editPos(@Validated @RequestBody PosRequest request) {
        environmentService.moveNode(request);
    }

    @GetMapping("/get-options/{projectId}")
    @Operation(summary = "项目管理-环境-环境目录-列表")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<EnvironmentOptionsDTO> list(@PathVariable String projectId) {
        return environmentService.listOption(projectId);
    }
}
