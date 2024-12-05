package io.metersphere.api.controller.scenario;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiResource;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.ApiScenarioBatchExportRequest;
import io.metersphere.api.dto.definition.ExecutePageRequest;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.ApiScenarioDataTransferService;
import io.metersphere.api.service.ApiValidateService;
import io.metersphere.api.service.scenario.ApiScenarioLogService;
import io.metersphere.api.service.scenario.ApiScenarioNoticeService;
import io.metersphere.api.service.scenario.ApiScenarioRunService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.file.annotation.FileLimit;
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
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/scenario")
@Tag(name = "接口测试-接口场景管理")
public class ApiScenarioController {
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiScenarioRunService apiScenarioRunService;
    @Resource
    private ApiValidateService apiValidateService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiScenarioDataTransferService apiScenarioDataTransferService;

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "pos desc, id desc");
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request, true, null));
    }

    @PostMapping("/statistics")
    @Operation(summary = "接口测试-接口场景管理-获取通过率")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    public List<ApiScenarioDTO> selectTestPlanMetricById(@RequestBody List<String> ids) {
        return apiScenarioService.calculateRate(ids);
    }

    @PostMapping("/trash/page")
    @Operation(summary = "接口测试-接口场景管理-场景列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiScenarioDTO>> getTrashPage(@Validated @RequestBody ApiScenarioPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "delete_time desc, id desc");
        request.setDeleted(true);
        return PageUtils.setPageInfo(page, apiScenarioService.getScenarioPage(request, true, null));
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
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.addScenarioDTO(#request)", targetClass = ApiScenarioNoticeService.class)
    public ApiScenario add(@Validated @RequestBody ApiScenarioAddRequest request) {
        return apiScenarioService.add(request, SessionUtils.getUserId());
    }

    @FileLimit
    @PostMapping("/upload/temp/file")
    @Operation(summary = "接口测试-接口场景管理-上传场景所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_SCENARIO_ADD, PermissionConstants.PROJECT_API_SCENARIO_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
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
    @Log(type = OperationLogType.DELETE, expression = "#msClass.moveToGcLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    @SendNotice(taskType = NoticeConstants.TaskType.API_SCENARIO_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getScenarioDTO(#id)", targetClass = ApiScenarioNoticeService.class)
    public void deleteToGc(@PathVariable String id) {
        apiScenarioService.deleteToGc(id, SessionUtils.getUserId());
    }

    @GetMapping("/get/{scenarioId}")
    @Operation(summary = "接口测试-接口场景管理-获取场景详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#scenarioId", resourceType = "api_scenario")
    public ApiScenarioDetailDTO getApiScenarioDetailDTO(@PathVariable String scenarioId) {
        return apiScenarioService.getApiScenarioDetailDTO(scenarioId, SessionUtils.getUserId());
    }

    @PostMapping("/step/get/un-save")
    @Operation(summary = "接口测试-接口场景管理-获取未保存的场景步骤详情")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_UPDATE, PermissionConstants.PROJECT_API_SCENARIO_ADD},
            logical = Logical.OR)
    public Object getUnsavedStepDetail(@Validated @RequestBody ApiScenarioStepDetailRequest request) {
        return JSON.parseObject(JSON.toJSONString(apiScenarioService.getUnsavedStepDetail(request)));
    }

    @GetMapping("/step/get/{stepId}")
    @Operation(summary = "接口测试-接口场景管理-获取场景步骤详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#stepId", resourceType = "api_scenario_step")
    public Object getStepDetail(@PathVariable String stepId) {
        return JSON.parseObject(JSON.toJSONString(apiScenarioService.getStepDetail(stepId)));
    }

    @PostMapping("/step/file/copy")
    @Operation(summary = "接口测试-接口场景管理-复制步骤时，复制步骤文件")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_UPDATE, PermissionConstants.PROJECT_API_SCENARIO_ADD},
            logical = Logical.OR)
    public Map<String, String> copyStepFile(@Validated @RequestBody ApiScenarioStepFileCopyRequest request) {
        return apiScenarioService.copyStepFile(request);
    }

    @GetMapping("/step/resource-info/{resourceId}")
    @Operation(summary = "接口测试-接口场景管理-获取步骤关联资源的信息")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    public ApiStepResourceInfo getStepResourceInfo(@PathVariable String resourceId, @RequestParam String resourceType) {
        return apiScenarioService.getStepResourceInfo(resourceId, resourceType);
    }

    @GetMapping("/recover/{id}")
    @Operation(summary = "接口测试-接口场景管理-恢复场景")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @Log(type = OperationLogType.RESTORE, expression = "#msClass.restoreLog(#id)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public void recover(@PathVariable String id) {
        apiValidateService.validateApiMenuInProject(id, ApiResource.API_SCENARIO.name());
        apiScenarioService.recover(id);
    }

    @PostMapping("/debug")
    @Operation(summary = "接口测试-接口场景管理-场景调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    public TaskRequestDTO debug(@Validated @RequestBody ApiScenarioDebugRequest request) {
        return apiScenarioRunService.debug(request);
    }

    @PostMapping("/run")
    @Operation(summary = "接口测试-接口场景管理-场景执行")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    public TaskRequestDTO run(@Validated @RequestBody ApiScenarioDebugRequest request) {
        return apiScenarioRunService.run(request, SessionUtils.getUserId());
    }

    @GetMapping("/run/{id}")
    @Operation(summary = "接口测试-接口场景管理-场景执行")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario")
    public TaskRequestDTO run(@PathVariable String id, @RequestParam(required = false) String reportId) {
        return apiScenarioRunService.run(id, reportId, SessionUtils.getUserId());
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
    public String scheduleConfig(@Validated @RequestBody ApiScenarioScheduleConfigRequest request) {
        apiValidateService.validateApiMenuInProject(request.getScenarioId(), ApiResource.API_SCENARIO.name());
        return apiScenarioService.scheduleConfig(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/schedule-config-delete/{scenarioId}")
    @Operation(summary = "接口测试-接口场景管理-删除定时任务配置")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.scheduleLog(#scenarioId)", msClass = ApiScenarioLogService.class)
    @CheckOwner(resourceId = "#scenarioId", resourceType = "api_scenario")
    public void deleteScheduleConfig(@PathVariable String scenarioId) {
        apiValidateService.validateApiMenuInProject(scenarioId, ApiResource.API_SCENARIO.name());
        apiScenarioService.deleteScheduleConfig(scenarioId);
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

    @PostMapping(value = "/get/system-request")
    @Operation(summary = "接口测试-接口场景管理-获取系统请求")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_SCENARIO_READ, PermissionConstants.PROJECT_API_SCENARIO_ADD, PermissionConstants.PROJECT_API_SCENARIO_UPDATE}, logical = Logical.OR)
    public List<ApiScenarioStepDTO> getSystemRequest(@Validated @RequestBody ApiScenarioSystemRequest request) {
        return apiScenarioService.getSystemRequest(request);
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "接口测试-接口场景管理-场景-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.getTargetId()", resourceType = "api_scenario")
    public void editPos(@Validated @RequestBody PosRequest request) {
        apiScenarioService.moveNode(request);
    }

    @PostMapping("/execute/page")
    @Operation(summary = "接口测试-接口场景管理-场景-获取执行历史")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_SCENARIO_READ, PermissionConstants.PROJECT_API_SCENARIO_UPDATE})
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_scenario")
    public Pager<List<ExecuteReportDTO>> getExecuteList(@Validated @RequestBody ExecutePageRequest request) {
        String sort = StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "eti.create_time desc";
        if (StringUtils.isNotBlank(sort)) {
            sort = sort.replace("start_time", "eti.create_time");
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), sort);
        return PageUtils.setPageInfo(page, apiScenarioService.getExecuteList(request));
    }

    @PostMapping("/operation-history/page")
    @Operation(summary = "接口测试-接口场景管理-场景-接口变更历史")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_SCENARIO_READ, PermissionConstants.PROJECT_API_SCENARIO_UPDATE})
    @CheckOwner(resourceId = "#request.getSourceId()", resourceType = "api_scenario")
    public Pager<List<OperationHistoryDTO>> operationHistoryList(@Validated @RequestBody OperationHistoryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiScenarioService.operationHistoryList(request));
    }

    @PostMapping("/transfer")
    @Operation(summary = "接口测试-接口场景管理-场景-附件-文件转存")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String transfer(@Validated @RequestBody ApiTransferRequest request) {
        return apiScenarioService.scenarioTransfer(request, SessionUtils.getUserId());
    }

    @PostMapping("/step/transfer")
    @Operation(summary = "接口测试-接口场景管理-场景步骤-附件-文件转存")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String stepTransfer(@Validated @RequestBody ApiTransferRequest request) {
        return apiScenarioService.stepTransfer(request, SessionUtils.getUserId());
    }

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "接口测试-接口场景管理-场景-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/get-reference")
    @Operation(summary = "接口测试-接口场景管理-场景-引用关系")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.getResourceId()", resourceType = "api_scenario")
    public Pager<List<ReferenceDTO>> getReference(@Validated @RequestBody ReferenceRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "id desc");
        return PageUtils.setPageInfo(page, apiScenarioService.getReference(request));
    }


    @PostMapping(value = "/export/{type}")
    @Operation(summary = "接口测试-接口场景管理-场景-导出场景")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String export(@RequestBody ApiScenarioBatchExportRequest request, @PathVariable String type) {
        return apiScenarioDataTransferService.exportScenario(request, type, SessionUtils.getUserId());
    }


    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_IMPORT)
    @Operation(summary = "接口测试-接口场景管理-场景-导入场景")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiScenarioImportRequest request) {
        request.setOperator(SessionUtils.getUserId());
        apiScenarioDataTransferService.importScenario(file, request);
    }

    @GetMapping("/stop/{taskId}")
    @Operation(summary = "接口测试-接口场景管理-导出-停止导出")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXPORT)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void caseStopExport(@PathVariable String taskId) {
        apiScenarioDataTransferService.stopExport(taskId, SessionUtils.getUserId());
    }

    @GetMapping(value = "/download/file/{projectId}/{fileId}")
    @Operation(summary = "接口测试-接口场景管理-下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_EXPORT)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void downloadImgById(@PathVariable String projectId, @PathVariable String fileId, HttpServletResponse httpServletResponse) {
        apiScenarioDataTransferService.downloadFile(projectId, fileId, SessionUtils.getUserId(), httpServletResponse);
    }
}
