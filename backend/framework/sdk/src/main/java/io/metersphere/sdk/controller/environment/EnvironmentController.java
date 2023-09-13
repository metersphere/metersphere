package io.metersphere.sdk.controller.environment;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.environment.EnvironmentRequest;
import io.metersphere.sdk.dto.environment.datasource.DataSource;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.service.environment.EnvironmentService;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping(value = "/project/environment")
@Tag(name = "项目管理-环境")
public class EnvironmentController {

    @Resource
    private PluginLoadService pluginLoadService;

    @Resource
    private EnvironmentService environmentService;


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
    public EnvironmentRequest add(@Validated({Created.class}) @RequestBody EnvironmentRequest environmentRequest,
                                  @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        return environmentService.add(environmentRequest, SessionUtils.getUserId(), sslFiles);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE)
    @Operation(summary = "项目管理-环境-环境目录-修改")
    public void update() {

    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "项目管理-环境-环境目录-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE)
    public void delete(@PathVariable String id) {
        environmentService.delete(id);
    }

    @PostMapping("/database/validate")
    @Operation(summary = "项目管理-环境-数据库配置-校验")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public void validate(@RequestBody DataSource databaseConfig) {
        try {
            if (StringUtils.isNotBlank(databaseConfig.getDriverId())) {
                ClassLoader classLoader = pluginLoadService.getClassLoader(databaseConfig.getDriverId());
                Driver driver = (Driver) classLoader.loadClass(databaseConfig.getDriver()).newInstance();
                Properties properties = new Properties();
                properties.setProperty("user", databaseConfig.getUsername());
                properties.setProperty("password", databaseConfig.getPassword());
                driver.connect(databaseConfig.getDbUrl(), properties);
            } else {
                DriverManager.getConnection(databaseConfig.getDbUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/database/driver-options/{organizationId}")
    @Operation(summary = "项目管理-环境-数据库配置-数据库驱动选项")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ)
    public Map<String, String> driverOptions(@PathVariable String organizationId) {
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
    public List<EnvironmentRequest> export(@RequestBody List<String> environmentIds) {
        return environmentService.export(environmentIds);
    }

}
