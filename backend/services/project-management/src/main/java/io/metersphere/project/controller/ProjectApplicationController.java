package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.ModuleDTO;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
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

    private static final String UNDERLINE = "_";

    /**
     * ==========测试计划==========
     */

    @PostMapping("/update/test-plan")
    @Operation(summary = "测试计划-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateTestPlanLog(#application)", msClass = ProjectApplicationService.class)
    public void updateTestPlan(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        projectApplicationService.update(application, SessionUtils.getUserId());
    }

    @PostMapping("/test-plan")
    @Operation(summary = "测试计划-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TEST_PLAN_READ)
    public Map<String, Object> getTestPlan(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.TEST_PLAN.values()).stream().map(ProjectApplicationType.TEST_PLAN::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }

    /**
     * ==========接口测试==========
     */

    @PostMapping("/update/api")
    @Operation(summary = "接口测试-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateApiLog(#application)", msClass = ProjectApplicationService.class)
    public void updateApi(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        projectApplicationService.update(application, SessionUtils.getUserId());
    }

    @PostMapping("/api")
    @Operation(summary = "接口测试-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_API_READ)
    public Map<String, Object> getApi(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.stream(ProjectApplicationType.API.values()).map(ProjectApplicationType.API::name).collect(Collectors.toList());
        Map<String, Object> configMap = projectApplicationService.get(request, types);
        int errorNum = projectApplicationService.getFakeErrorList(request.getProjectId());
        configMap.put("FAKE_ERROR_NUM", errorNum);
        int enableErrorNum = projectApplicationService.getEnableFakeErrorList(request.getProjectId());
        configMap.put("ENABLE_FAKE_ERROR_NUM", enableErrorNum);
        return configMap;
    }

    /**
     * ==========任务中心开始==========
     */
    @PostMapping("/update/task")
    @Operation(summary = "任务中心-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TASK_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateTaskLog(#application)", msClass = ProjectApplicationService.class)
    public void updateTask(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        projectApplicationService.update(application, SessionUtils.getUserId());
    }

    @PostMapping("/task")
    @Operation(summary = "任务中心-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_TASK_READ)
    public Map<String, Object> getTask(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.stream(ProjectApplicationType.TASK.values()).map(ProjectApplicationType.TASK::name).collect(Collectors.toList());
        return projectApplicationService.get(request, types);
    }
    /**
     * ==========任务中心结束==========
     */

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
        return projectService.getPoolOptions(projectId);
    }


    /**
     * ==========用例管理==========
     */

    @PostMapping("/update/case")
    @Operation(summary = "用例管理-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateCaseLog(#application)", msClass = ProjectApplicationService.class)
    public void updateCase(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        if ((ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + UNDERLINE + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name()).equals(application.getType())) {
            String projectDemandThirdPartConfig = projectApplicationService.getProjectDemandThirdPartConfig(application.getProjectId());
            if (StringUtils.isBlank(projectDemandThirdPartConfig)) {
                throw new MSException(Translator.get("third_part_config_is_null"));
            }
        }
        projectApplicationService.update(application, SessionUtils.getUserId());
    }


    @PostMapping("/case")
    @Operation(summary = "用例管理-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_READ)
    public Map<String, Object> getCase(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.CASE.values()).stream().map(ProjectApplicationType.CASE::name).collect(Collectors.toList());
        types.add(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name());
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
        return projectApplicationService.getDemandPluginScript(pluginId);
    }


    @PostMapping("/update/case/related/{projectId}")
    @Operation(summary = "用例管理-关联需求")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateRelatedRequirementsLog(#projectId, #configs)", msClass = ProjectApplicationService.class)
    public void updateRelated(@PathVariable("projectId") String projectId, @RequestBody Map<String, String> configs) {
        projectApplicationService.updateRelated(projectId, configs, SessionUtils.getUserId());
    }

    @GetMapping("/case/related/info/{projectId}")
    @Operation(summary = "用例管理-获取关联需求信息")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.PROJECT_APPLICATION_CASE_READ})
    public Map<String, String> getRelatedConfigInfo(@PathVariable("projectId") String projectId) {
        return projectApplicationService.getRelatedConfigInfo(projectId);
    }

    /**
     * ==========缺陷管理==========
     */

    @PostMapping("/update/bug")
    @Operation(summary = "缺陷管理-配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateBugLog(#application)", msClass = ProjectApplicationService.class)
    public void updateBug(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        if ((ProjectApplicationType.BUG.BUG_SYNC.name() + UNDERLINE + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name()).equals(application.getType())) {
            String projectBugThirdPartConfig = projectApplicationService.getProjectBugThirdPartConfig(application.getProjectId());
            if (StringUtils.isBlank(projectBugThirdPartConfig)) {
                throw new MSException(Translator.get("third_part_config_is_null"));
            }
        }
        projectApplicationService.update(application, SessionUtils.getUserId());
    }

    @PostMapping("/bug")
    @Operation(summary = "缺陷管理-获取配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_READ)
    public Map<String, Object> getBug(@Validated @RequestBody ProjectApplicationRequest request) {
        List<String> types = Arrays.asList(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name());
        return projectApplicationService.get(request, types);
    }

    @GetMapping("/bug/platform/{organizationId}")
    @Operation(summary = "缺陷管理-获取平台下拉框列表")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_READ)
    public List<OptionDTO> getBugPlatformOptions(@PathVariable String organizationId) {
        return projectApplicationService.getPlatformOptions(organizationId);
    }


    @GetMapping("/bug/platform/info/{pluginId}")
    @Operation(summary = "缺陷管理-选择平台获取平台信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_READ)
    public Object getBugPlatformInfo(@PathVariable String pluginId) {
        return projectApplicationService.getBugPluginScript(pluginId);
    }


    @PostMapping("/update/bug/sync/{projectId}")
    @Operation(summary = "缺陷管理-同步缺陷配置")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateBugSyncLog(#projectId, #configs)", msClass = ProjectApplicationService.class)
    public void syncBugConfig(@PathVariable("projectId") String projectId, @RequestBody Map<String, Object> configs) {
        projectApplicationService.syncBugConfig(projectId, configs, SessionUtils.getUserId());
    }


    @GetMapping("/bug/sync/info/{projectId}")
    @Operation(summary = "缺陷管理-获取同步缺陷信息")
    @RequiresPermissions(PermissionConstants.PROJECT_APPLICATION_BUG_READ)
    public Map<String, String> getBugConfigInfo(@PathVariable("projectId") String projectId) {
        return projectApplicationService.getBugConfigInfo(projectId);
    }


    @GetMapping("/module-setting/{projectId}")
    @Operation(summary = "获取菜单列表")
    public List<ModuleDTO> getModuleSetting(@PathVariable String projectId) {
        return projectApplicationService.getModuleSetting(projectId);
    }


    @PostMapping("/validate/{pluginId}")
    @Operation(summary = "插件key校验")
    public void validateProjectConfig(@PathVariable("pluginId") String pluginId, @RequestBody Map configs) {
        projectApplicationService.validateProjectConfig(pluginId, configs, SessionUtils.getCurrentOrganizationId());
    }

}
