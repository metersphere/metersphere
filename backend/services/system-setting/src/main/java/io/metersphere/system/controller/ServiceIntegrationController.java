package io.metersphere.system.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.ServiceIntegrationDTO;
import io.metersphere.system.dto.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.ServiceIntegrationLogService;
import io.metersphere.system.service.ServiceIntegrationService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-8-9
 */
@RestController
@RequestMapping("/service/integration")
@Tag(name = "系统设置-组织-服务集成")
public class ServiceIntegrationController {

    @Resource
    private ServiceIntegrationService serviceIntegrationService;

    @GetMapping("/list/{organizationId}")
    @Operation(summary = "系统设置-组织-服务集成-获取服务集成列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ)
    public List<ServiceIntegrationDTO> list(@PathVariable String organizationId) {
        return serviceIntegrationService.list(organizationId);
    }

    @PostMapping("/add")
    @Operation(summary = "系统设置-组织-服务集成-创建服务集成")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#request)", msClass = ServiceIntegrationLogService.class)
    public ServiceIntegration add(@Validated({Created.class}) @RequestBody ServiceIntegrationUpdateRequest request) {
        return serviceIntegrationService.add(request);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-组织-服务集成-更新服务集成")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = ServiceIntegrationLogService.class)
    public ServiceIntegration update(@Validated({Updated.class}) @RequestBody ServiceIntegrationUpdateRequest request) {
        return serviceIntegrationService.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-组织-服务集成-删除服务集成")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ServiceIntegrationLogService.class)
    public void delete(@PathVariable String id) {
        serviceIntegrationService.delete(id);
    }

    @PostMapping("/validate/{pluginId}/{orgId}")
    @Operation(summary = "系统设置-组织-服务集成-校验服务集成信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE)
    public void validate(@PathVariable String pluginId,
                         @PathVariable String orgId,
                         @Validated({Updated.class})
                         @RequestBody
                         @NotEmpty
                         @Schema(description = "配置的表单键值对", requiredMode = Schema.RequiredMode.REQUIRED)
                         HashMap<String, String> serviceIntegrationInfo) {
        serviceIntegrationService.validate(pluginId, orgId, serviceIntegrationInfo);
    }

    @GetMapping("/validate/{id}")
    @Operation(summary = "系统设置-组织-服务集成-校验服务集成信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_UPDATE)
    public void validate(@PathVariable String id) {
        serviceIntegrationService.validate(id);
    }

    @GetMapping("/script/{pluginId}")
    @Operation(summary = "系统设置-组织-服务集成-获取前端配置脚本")
    @RequiresPermissions(PermissionConstants.SYSTEM_SERVICE_INTEGRATION_READ)
    public Object getPluginScript(@PathVariable String pluginId) {
        return serviceIntegrationService.getPluginScript(pluginId);
    }
}