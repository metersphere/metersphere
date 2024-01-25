package io.metersphere.api.controller.scenario;

import io.metersphere.api.constants.ApiResource;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.ApiScenarioBatchCopyRequest;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.service.ApiValidateService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/scenario/batch-operation")
@Tag(name = "接口测试-接口场景批量操作")
public class ApiScenarioBatchOperationController {
    @Resource
    private ApiValidateService apiValidateService;
    @Resource
    private ApiScenarioService apiScenarioService;

    @PostMapping("/edit")
    @Operation(summary = "接口测试-接口场景批量操作-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchUpdate(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        apiScenarioService.batchEdit(request, SessionUtils.getUserId());
    }

    /*
    todo:
        @PostMapping("/gc-batch-delete")
        @Operation(summary = "接口测试-接口场景批量操作-回收站列表-批量删除")
        @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
        @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
        @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
        public void deleteFromGC(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
            apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
            apiScenarioService.deleteFromGC(request, SessionUtils.getUserId());
        }


    todo
        @PostMapping("/gc-batch-restore")
        @Operation(summary = "接口测试-接口场景批量操作-回收站列表-批量恢复")
        @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
        @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
        public void restoreFromGC(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
            apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
            apiScenarioService.restoreFromGC(request, SessionUtils.getUserId());
        }



    todo
        @PostMapping("/batch-delete")
        @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量删除")
        @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
        @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
        public void batchDelete(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
            apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
            apiScenarioService.batchDelete(request, SessionUtils.getUserId());
        }



    todo
        @PostMapping("/batch-move")
        @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量移动")
        @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
        @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
        public void batchMove(@Validated @RequestBody ApiScenarioBatchEditRequest request) {
            apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
            apiScenarioService.batchMove(request, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/batch-move", HttpMethodConstants.POST.name()));
        }
    */


    @PostMapping("/batch-copy")
    @Operation(summary = "接口测试-接口场景批量操作-场景列表操作-批量复制")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiScenarioBatchOperationResponse batchCopy(@Validated @RequestBody ApiScenarioBatchCopyRequest request) {
        apiValidateService.validateApiMenuInProject(request.getProjectId(), ApiResource.PROJECT.name());
        return apiScenarioService.batchCopy(request, new LogInsertModule(SessionUtils.getUserId(), "/api/scenario/batch-operation/batch-copy", HttpMethodConstants.POST.name()));
    }

}
