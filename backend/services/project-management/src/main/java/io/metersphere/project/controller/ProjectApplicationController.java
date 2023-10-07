package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.ModuleDTO;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ModuleType;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.domain.User;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Tag(name = "项目管理-项目与权限-菜单管理")
@RestController
@RequestMapping("/project/application")
public class ProjectApplicationController {
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Resource
    private ProjectService projectService;

    /**
     * ==========测试计划==========
     */

    @PostMapping("/update/test-plan")
    @Operation(summary = "测试计划-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateTestPlanLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateTestPlan(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/test-plan")
    @Operation(summary = "测试计划-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_READ)
    public List<ProjectApplication> getTestPlan(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.TEST_PLAN.values()).stream().map(ProjectApplicationType.TEST_PLAN::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }


    /**
     * ==========UI测试==========
     */

    @PostMapping("/update/ui")
    @Operation(summary = "UI测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateUiLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateUI(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/ui")
    @Operation(summary = "UI测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_READ)
    public List<ProjectApplication> getUI(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.UI.values()).stream().map(ProjectApplicationType.UI::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }

    @GetMapping("/ui/resource/pool/{projectId}")
    @Operation(summary = "UI测试-获取资源池列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_READ)
    public List<OptionDTO> getUiPoolOptions(@PathVariable String projectId) {
        return projectService.getPoolOptions(projectId, ModuleType.UI_TEST);
    }


    /**
     * ==========性能测试==========
     */

    @PostMapping("/update/performance-test")
    @Operation(summary = "性能测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePerformanceLog(#applications)", msClass = ProjectApplicationService.class)
    public void updatePerformanceTest(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/performance-test")
    @Operation(summary = "性能测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_READ)
    public List<ProjectApplication> getPerformanceTest(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.PERFORMANCE_TEST.values()).stream().map(ProjectApplicationType.PERFORMANCE_TEST::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }

    @GetMapping("/performance-test/user/{projectId}")
    @Operation(summary = "性能测试-获取审核人")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_READ)
    public List<User> getReviewerUser(@PathVariable String projectId) {
        return projectApplicationService.getProjectUserList(StringUtils.defaultIfBlank(projectId, SessionUtils.getCurrentProjectId()));
    }


    /**
     * ==========接口测试==========
     */

    @PostMapping("/update/api")
    @Operation(summary = "接口测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateApiLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateApi(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/api")
    @Operation(summary = "接口测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<ProjectApplication> getApi(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.API.values()).stream().map(ProjectApplicationType.API::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }

    @GetMapping("/api/user/{projectId}")
    @Operation(summary = "接口测试-获取审核人")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<User> getApiReviewerUser(@PathVariable String projectId) {
        return projectApplicationService.getProjectUserList(StringUtils.defaultIfBlank(projectId, SessionUtils.getCurrentProjectId()));
    }


    @GetMapping("/api/resource/pool/{projectId}")
    @Operation(summary = "接口测试-获取资源池列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<OptionDTO> getApiPoolOptions(@PathVariable String projectId) {
        return projectService.getPoolOptions(projectId, ModuleType.API_TEST);
    }


    /**
     * ==========用例管理==========
     */

    @PostMapping("/update/case")
    @Operation(summary = "用例管理-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateCaseLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateCase(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }


    @PostMapping("/case")
    @Operation(summary = "用例管理-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public List<ProjectApplication> getCase(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.CASE.values()).stream().map(ProjectApplicationType.CASE::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }


    @GetMapping("/case/platform/{organizationId}")
    @Operation(summary = "用例管理-获取平台下拉框列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public List<OptionDTO> getCasePlatformOptions(@PathVariable String organizationId) {
        return projectApplicationService.getPlatformOptions(organizationId);
    }


    @GetMapping("/case/platform/info/{pluginId}")
    @Operation(summary = "用例管理-选择平台获取平台信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public Object getCasePlatformInfo(@PathVariable String pluginId) {
        return projectApplicationService.getPluginScript(pluginId);
    }


    @PostMapping("/update/case/related/{projectId}")
    @Operation(summary = "用例管理-关联需求")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateRelatedRequirementsLog(#projectId, #configs)", msClass = ProjectApplicationService.class)
    public void updateRelated(@PathVariable("projectId") String projectId, @RequestBody Map<String, String> configs) {
        projectApplicationService.updateRelated(projectId, configs);
    }

    @GetMapping("/case/related/info/{projectId}")
    @Operation(summary = "用例管理-获取关联需求信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_READ)
    public Map<String, String> getRelatedConfigInfo(@PathVariable("projectId") String projectId) {
        return projectApplicationService.getRelatedConfigInfo(projectId);
    }


    /**
     * ==========工作台==========
     */

    @PostMapping("/update/workstation")
    @Operation(summary = "工作台-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_WORKSTATION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateWorkstationLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateWorkstation(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/workstation")
    @Operation(summary = "工作台-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_WORKSTATION_READ)
    public List<ProjectApplication> getWorkstation(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.WORKSTATION.values()).stream().map(ProjectApplicationType.WORKSTATION::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }


    /**
     * ==========缺陷管理==========
     */

    @PostMapping("/update/issue")
    @Operation(summary = "缺陷管理-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateWorkstationLog(#applications)", msClass = ProjectApplicationService.class)
    public void updateIssue(@Validated({Updated.class}) @RequestBody List<ProjectApplication> applications) {
        projectApplicationService.update(applications, SessionUtils.getUserId());
    }

    @PostMapping("/issue")
    @Operation(summary = "缺陷管理-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_READ)
    public List<ProjectApplication> getIssue(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.WORKSTATION.values()).stream().map(ProjectApplicationType.WORKSTATION::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }

    @GetMapping("/issue/platform/{organizationId}")
    @Operation(summary = "缺陷管理-获取平台下拉框列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_READ)
    public List<OptionDTO> getIssuePlatformOptions(@PathVariable String organizationId) {
        return projectApplicationService.getPlatformOptions(organizationId);
    }


    @GetMapping("/issue/platform/info/{pluginId}")
    @Operation(summary = "缺陷管理-选择平台获取平台信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_READ)
    public Object getIssuePlatformInfo(@PathVariable String pluginId) {
        return projectApplicationService.getPluginScript(pluginId);
    }


    @PostMapping("/update/issue/sync/{projectId}")
    @Operation(summary = "缺陷管理-同步缺陷配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateIssueSyncLog(#projectId, #configs)", msClass = ProjectApplicationService.class)
    public void syncIssueConfig(@PathVariable("projectId") String projectId, @RequestBody Map<String, String> configs) {
        projectApplicationService.syncIssueConfig(projectId, configs);
    }


    @GetMapping("/issue/sync/info/{projectId}")
    @Operation(summary = "缺陷管理-获取同步缺陷信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_ISSUE_READ)
    public Map<String, String> getIssueConfigInfo(@PathVariable("projectId") String projectId) {
        return projectApplicationService.getIssueConfigInfo(projectId);
    }


    @GetMapping("/module-setting/{projectId}")
    @Operation(summary = "获取菜单列表")
    public List<ModuleDTO> getModuleSetting(@PathVariable String projectId) {
        return projectApplicationService.getModuleSetting(projectId);
    }


    @PostMapping("/validate/{pluginId}")
    @Operation(summary = "插件key校验")
    public void validateProjectConfig(@PathVariable("pluginId") String pluginId, @RequestBody Map configs) {
        projectApplicationService.validateProjectConfig(pluginId, configs);
    }

}
