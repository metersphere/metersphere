package io.metersphere.api.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  10:54
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "接口测试")
public class ApiTestController {

    @Resource
    private ApiTestService apiTestService;
    @Resource
    private ApiExecuteService apiExecuteService;

    @GetMapping("/protocol/{organizationId}")
    @Operation(summary = "获取协议插件的的协议列表")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<ProtocolDTO> getProtocols(@PathVariable String organizationId) {
        return apiTestService.getProtocols(organizationId);
    }

    @PostMapping("/mock")
    @Operation(summary = "获取mock数据")
    public String mock(@RequestBody TextNode key) {
        if (key == null || key.asText().isEmpty()) {
            return "";
        }
        return Mock.calculate(key.asText()).toString();
    }

    @PostMapping("/custom/func/run")
    @Operation(summary = "项目管理-公共脚本-脚本测试")
    @RequiresPermissions(PermissionConstants.PROJECT_CUSTOM_FUNCTION_EXECUTE)
    public TaskRequestDTO run(@Validated @RequestBody CustomFunctionRunRequest runRequest) {
        return apiExecuteService.runScript(runRequest);
    }

    @GetMapping("/plugin/script/{pluginId}")
    @Operation(summary = "获取协议插件的的协议列表")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_DEBUG_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    public Object getApiProtocolScript(@PathVariable String pluginId) {
        return apiTestService.getApiProtocolScript(pluginId);
    }

    @PostMapping("/plugin/form/option")
    @Operation(summary = "接口测试-获取插件表单选项")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_DEBUG_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    public List<ApiPluginSelectOption> getFormOptions(@Validated @RequestBody ApiTestPluginOptionRequest request) {
        return apiTestService.getFormOptions(request);
    }

    @GetMapping("/env-list/{projectId}")
    @Operation(summary = "接口测试-环境列表")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<Environment> getEnvList(@PathVariable String projectId) {
        return apiTestService.getEnvList(projectId);
    }

    @GetMapping("/environment/{environmentId}")
    @Operation(summary = "接口测试-获取环境中数据源等参数")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#environmentId", resourceType = "environment")
    public EnvironmentConfig getEnvironmentConfig(@PathVariable String environmentId) {
        return apiTestService.getEnvironmentConfig(environmentId);
    }

    @GetMapping("/pool-option/{projectId}")
    @Operation(summary = "接口测试-获取资源池")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<TestResourcePool> getPool(@PathVariable String projectId) {
        return apiTestService.getPoolOption(projectId);
    }

    @GetMapping("/get-pool/{projectId}")
    @Operation(summary = "接口测试-获取资源池")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public String getPoolId(@PathVariable String projectId) {
        return apiTestService.getPoolId(projectId);
    }

    @PostMapping("/download")
    @Operation(summary = "执行结果附件下载")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_SCENARIO_EXECUTE,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE,
            PermissionConstants.PROJECT_API_DEBUG_EXECUTE,
            PermissionConstants.PROJECT_API_REPORT_READ,
    }, logical = Logical.OR)
    public void download(@RequestBody TextNode path, HttpServletResponse response) throws Exception {
        // 不属于当前项目的文件不允许下载
        if (!StringUtils.contains(path.asText(), SessionUtils.getCurrentProjectId())) {
            return;
        }
        apiTestService.download(path.asText(), response);
    }

    @GetMapping("/common-script/{scriptId}")
    @Operation(summary = "获取最新的公共脚本信息")
    @RequiresPermissions(value = {
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_DEBUG_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ
    }, logical = Logical.OR)
    public CommonScriptInfo getCommonScriptInfo(@PathVariable String scriptId) {
        return apiTestService.getCommonScriptInfo(scriptId);
    }
}
