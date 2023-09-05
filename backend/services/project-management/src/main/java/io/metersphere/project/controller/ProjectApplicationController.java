package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
}
