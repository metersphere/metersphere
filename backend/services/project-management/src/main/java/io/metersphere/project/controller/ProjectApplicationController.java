package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "项目管理-应用设置")
@RestController
@RequestMapping("/project/application")
public class ProjectApplicationController {
    @Resource
    private ProjectApplicationService projectApplicationService;


    /**
     * ==========测试计划==========
     */

    @PostMapping("/update/test-plan")
    @Operation(summary = "应用设置-测试计划-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateTestPlanLog(#application)", msClass = ProjectApplicationService.class)
    public ProjectApplication updateTestPlan(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }

    @PostMapping("/test-plan")
    @Operation(summary = "应用设置-测试计划-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_READ)
    public List<ProjectApplication> getTestPlan(@Validated @RequestBody ProjectApplicationRequest request) {
        return projectApplicationService.get(request);
    }


    /**
     * ==========UI测试==========
     */

    @PostMapping("/update/ui")
    @Operation(summary = "应用设置-UI测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateUiLog(#application)", msClass = ProjectApplicationService.class)
    public ProjectApplication updateUI(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }

    @PostMapping("/ui")
    @Operation(summary = "应用设置-UI测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_READ)
    public List<ProjectApplication> getUI(@Validated @RequestBody ProjectApplicationRequest request) {
        return projectApplicationService.get(request);
    }

    @GetMapping("/ui/resource/pool/{organizationId}")
    @Operation(summary = "应用设置-UI测试-获取资源池列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_UI_READ)
    public List<OptionDTO> getUiResourcePoolList(@PathVariable String organizationId) {
        return projectApplicationService.getResourcePoolList(StringUtils.defaultIfBlank(organizationId, SessionUtils.getCurrentOrganizationId()));
    }


    /**
     * ==========性能测试==========
     */

    @PostMapping("/update/performance-test")
    @Operation(summary = "应用设置-性能测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updatePerformanceLog(#application)", msClass = ProjectApplicationService.class)
    public ProjectApplication updatePerformanceTest(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }

    @PostMapping("/performance-test")
    @Operation(summary = "应用设置-性能测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_READ)
    public List<ProjectApplication> getPerformanceTest(@Validated @RequestBody ProjectApplicationRequest request) {
        return projectApplicationService.get(request);
    }

    @GetMapping("/performance-test/user/{projectId}")
    @Operation(summary = "应用设置-性能测试-获取审核人")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_PERFORMANCE_TEST_READ)
    public List<User> getReviewerUser(@PathVariable String projectId) {
        return projectApplicationService.getProjectUserList(StringUtils.defaultIfBlank(projectId, SessionUtils.getCurrentProjectId()));
    }


    /**
     * ==========接口测试==========
     */

    @PostMapping("/update/api")
    @Operation(summary = "应用设置-接口测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateApiLog(#application)", msClass = ProjectApplicationService.class)
    public ProjectApplication updateApi(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }

    @PostMapping("/api")
    @Operation(summary = "应用设置-接口测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<ProjectApplication> getApi(@Validated @RequestBody ProjectApplicationRequest request) {
        return projectApplicationService.get(request);
    }

    @GetMapping("/api/user/{projectId}")
    @Operation(summary = "应用设置-接口测试-获取审核人")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<User> getApiReviewerUser(@PathVariable String projectId) {
        return projectApplicationService.getProjectUserList(StringUtils.defaultIfBlank(projectId, SessionUtils.getCurrentProjectId()));
    }


    @GetMapping("/api/resource/pool/{organizationId}")
    @Operation(summary = "应用设置-接口测试-获取资源池列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public List<OptionDTO> getResourcePoolList(@PathVariable String organizationId) {
        return projectApplicationService.getResourcePoolList(StringUtils.defaultIfBlank(organizationId, SessionUtils.getCurrentOrganizationId()));
    }



    /**
     * ==========用例管理==========
     */

    @PostMapping("/update/case")
    @Operation(summary = "应用设置-用例管理-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateCaseLog(#application)", msClass = ProjectApplicationService.class)
    public ProjectApplication updateCase(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }


    @PostMapping("/case")
    @Operation(summary = "应用设置-用例管理-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public List<ProjectApplication> getCase(@Validated @RequestBody ProjectApplicationRequest request) {
        return projectApplicationService.get(request);
    }


    @GetMapping("/case/platform/{organizationId}")
    @Operation(summary = "应用设置-用例管理-获取平台下拉框列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public List<OptionDTO> getCasePlatformOptions(@PathVariable String organizationId) {
        return projectApplicationService.getPlatformOptions(organizationId);
    }


    @GetMapping("case/platform/info/{pluginId}")
    @Operation(summary = "应用设置-用例管理-选择平台获取平台信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public Object getCasePlatformInfo(@PathVariable String pluginId) {
        return projectApplicationService.getPluginScript(pluginId);
    }

}
