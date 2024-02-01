package io.metersphere.api.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiResource;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.service.ApiValidateService;
import io.metersphere.api.service.scenario.ApiScenarioLogService;
import io.metersphere.api.service.scenario.ApiScenarioNoticeService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/scenario")
@Tag(name = "接口测试-接口场景管理")
public class ApiScenarioController {
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiValidateService apiValidateService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request));
    }

    @PostMapping("/trash/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getTrashPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        request.setDeleted(true);
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request));
    }

    @GetMapping("follow/{id}")
    @Operation(summary = "接口测试-接口场景管理-关注/ 取消关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public void follow(@PathVariable String id) {
        apiScenarioService.follow(id, SessionUtils.getUserId());
    }

    @PostMapping("/add")
    @Operation(summary = "接口测试-接口场景管理-创建场景")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiScenarioLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getScenarioDTO(#request)", targetClass = ApiScenarioNoticeService.class)
    public ApiScenario add(@Validated @RequestBody ApiScenarioAddRequest request) {
        return apiScenarioService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "接口测试-接口场景管理-上传场景所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_SCENARIO_ADD, PermissionConstants.PROJECT_API_SCENARIO_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiScenarioService.uploadTempFile(file);
    }

    @PostMapping("/update")
    @Operation(summary = "接口测试-接口场景管理-更新场景")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScenarioDTO(#request)", targetClass = ApiScenarioNoticeService.class)
    public ApiScenario update(@Validated @RequestBody ApiScenarioUpdateRequest request) {
        return apiScenarioService.update(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口场景管理-删除场景")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public void delete(@PathVariable String id) {
        apiScenarioService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/delete-to-gc/{id}")
    @Operation(summary = "接口测试-接口场景管理-删除场景到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getScenarioDTO(#id)", targetClass = ApiScenarioNoticeService.class)
    public void deleteToGc(@PathVariable String id) {
        apiScenarioService.deleteToGc(id);
    }

    @GetMapping("/get/{scenarioId}")
    @Operation(summary = "接口测试-接口场景管理-获取场景详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#scenarioId", resourceType = "api_scenario")
    public ApiScenarioDetail get(@PathVariable String scenarioId) {
        return apiScenarioService.get(scenarioId);
    }

    @GetMapping("/step/get/{stepId}")
    @Operation(summary = "接口测试-接口场景管理-获取场景步骤详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    public Object getStepDetail(@PathVariable String stepId) {
        return apiScenarioService.getStepDetail(stepId);
    }


    //需求补充：回收站里的相关操作都不需要发通知
    @GetMapping("/recover/{id}")
    @Operation(summary = "接口测试-接口场景管理-删除场景到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_RECOVER)
    @Log(type = OperationLogType.RESTORE, expression = "#msClass.restoreLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public void recover(@PathVariable String id) {
        apiValidateService.validateApiMenuInProject(id, ApiResource.API_SCENARIO.name());
        apiScenarioService.recover(id);
    }

    @PostMapping("/debug")
    @Operation(summary = "接口测试-接口场景管理-场景调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    public String debug(@Validated @RequestBody ApiScenarioDebugRequest request) {
        return apiScenarioService.debug(request);
    }

    @GetMapping(value = "/update-status/{id}/{status}")
    @Operation(summary = "接口测试-接口场景管理-更新状态")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScenarioDTO(#id)", targetClass = ApiScenarioNoticeService.class)
    public void updateStatus(@PathVariable String id, @PathVariable String status) {
        apiScenarioService.updateStatus(id, status, SessionUtils.getUserId());
    }

    @GetMapping(value = "/update-priority/{id}/{priority}")
    @Operation(summary = "接口测试-接口场景管理-更新等级")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScenarioDTO(#id)", targetClass = ApiScenarioNoticeService.class)
    public void updatePriority(@PathVariable String id, @PathVariable String priority) {
        apiScenarioService.updatePriority(id, priority, SessionUtils.getUserId());
    }

    @PostMapping(value = "/schedule-config")
    @Operation(summary = "接口测试-接口场景管理-定时任务配置")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.scheduleLog(#request.getScenarioId())", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#request.getScenarioId()", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.SCHEDULE_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getScheduleNotice(#request)", targetClass = ApiScenarioNoticeService.class)
    public String scheduleConfig(@Validated @RequestBody ApiScenarioScheduleConfigRequest request) {
        /*
        TODO to Chen Jianxing:
            request.configMap 中需要补充场景的执行信息，比如环境、资源池、是否失败停止等配置。
            在触发定时任务的APIScenarioScheduleJob中会用到
         */
        apiValidateService.validateApiMenuInProject(request.getScenarioId(), ApiResource.API_SCENARIO.name());
        return apiScenarioService.scheduleConfig(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/association/page")
    @Operation(summary = "接口测试-接口场景管理-场景引用关系列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.id", resourceType = "api_scenario")
    public Pager<List<ApiScenarioAssociationDTO>> getAssociationPage(@Validated @RequestBody ApiScenarioAssociationPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "resource_num desc");
        return PageUtils.setPageInfo(page, apiScenarioService.getAssociationPage(request));
    }
}
