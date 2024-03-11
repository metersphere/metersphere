package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.service.definition.ApiTestCaseLogService;
import io.metersphere.api.service.definition.ApiTestCaseNoticeService;
import io.metersphere.api.service.definition.ApiTestCaseRecoverService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.PosRequest;
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
@RequestMapping(value = "/api/case")
@Tag(name = "接口测试-接口管理-接口用例")
public class ApiTestCaseController {
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiTestCaseRecoverService apiTestCaseRecoverService;
    @Resource
    private FileModuleService fileModuleService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-接口用例-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiTestCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_CREATE, target = "#targetClass.getCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
    public ApiTestCase add(@Validated @RequestBody ApiTestCaseAddRequest request) {
        return apiTestCaseService.addCase(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/get-detail/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-获取详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public ApiTestCaseDTO get(@PathVariable String id) {
        return apiTestCaseService.get(id, SessionUtils.getUserId());
    }

    @GetMapping("/delete-to-gc/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-移动到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.moveToGcLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getCaseDTO(#id)", targetClass = ApiTestCaseNoticeService.class)
    public void deleteToGc(@PathVariable String id) {
        apiTestCaseService.deleteToGc(id, SessionUtils.getUserId());
    }

    @GetMapping("recover/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public void recover(@PathVariable String id) {
        apiTestCaseService.recover(id, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @GetMapping("follow/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.followLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public void follow(@PathVariable String id) {
        apiTestCaseService.follow(id, SessionUtils.getUserId());
    }

    @GetMapping("unfollow/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-取消关注")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.unfollowLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public void unfollow(@PathVariable String id) {
        apiTestCaseService.unfollow(id, SessionUtils.getUserId());
    }

    @GetMapping("delete/{id}")
    @Operation(summary = "接口测试-接口管理-接口用例-删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getCaseDTO(#request.id)", targetClass = ApiTestCaseNoticeService.class)
    public void delete(@PathVariable String id) {
        apiTestCaseService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-接口用例-更新")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiTestCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, target = "#targetClass.getCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
    @CheckOwner(resourceId = "#request.id", resourceType = "api_test_case")
    public ApiTestCase update(@Validated @RequestBody ApiTestCaseUpdateRequest request) {
        return apiTestCaseService.update(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/update-priority/{id}/{priority}")
    @Operation(summary = "接口测试-接口管理-接口用例-更新等级")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, target = "#targetClass.getCaseDTO(#id)", targetClass = ApiTestCaseNoticeService.class)
    public void updatePriority(@PathVariable String id, @PathVariable String priority) {
        apiTestCaseService.updatePriority(id, priority, SessionUtils.getUserId());
    }

    @GetMapping(value = "/update-status/{id}/{status}")
    @Operation(summary = "接口测试-接口管理-接口用例-更新状态")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = ApiTestCaseLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, target = "#targetClass.getCaseDTO(#id)", targetClass = ApiTestCaseNoticeService.class)
    public void updateStatus(@PathVariable String id, @PathVariable String status) {
        apiTestCaseService.updateStatus(id, status, SessionUtils.getUserId());
    }

    @PostMapping(value = "/page")
    @Operation(summary = "接口测试-接口管理-接口用例-分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public Pager<List<ApiTestCaseDTO>> page(@Validated @RequestBody ApiTestCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.page(request, false));
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "接口测试-接口管理-接口用例-批量删除")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    public void deleteBatchByParam(@RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/delete-to-gc")
    @Operation(summary = "接口测试-接口管理-接口用例-批量移动到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getBatchEditApiCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
    public void deleteToGcByParam(@RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseService.batchMoveGc(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/edit")
    @Operation(summary = "接口测试-接口管理-接口用例-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getBatchEditApiCaseDTO(#request)", targetClass = ApiTestCaseNoticeService.class)
    public void batchUpdate(@Validated @RequestBody ApiCaseBatchEditRequest request) {
        apiTestCaseService.batchEdit(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/recover")
    @Operation(summary = "接口测试-接口管理-接口用例-批量恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    public void batchRecover(@Validated @RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseRecoverService.batchRecover(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/trash/page")
    @Operation(summary = "接口测试-接口管理-接口用例-回收站-分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public Pager<List<ApiTestCaseDTO>> pageTrash(@Validated @RequestBody ApiTestCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.page(request, true));
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "接口测试-接口管理-接口用例-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    public void editPos(@Validated @RequestBody PosRequest request) {
        apiTestCaseService.moveNode(request);
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口调试所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiTestCaseService.uploadTempFile(file);
    }

    @PostMapping("/execute/page")
    @Operation(summary = "接口测试-接口管理-接口用例-获取执行历史")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_test_case")
    public Pager<List<ExecuteReportDTO>> getExecuteList(@Validated @RequestBody ExecutePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "start_time desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.getExecuteList(request));
    }

    @PostMapping("/operation-history/page")
    @Operation(summary = "接口测试-接口管理-接口用例-接口变更历史")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    @CheckOwner(resourceId = "#request.getSourceId()", resourceType = "api_test_case")
    public Pager<List<OperationHistoryDTO>> operationHistoryList(@Validated @RequestBody OperationHistoryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.operationHistoryList(request));
    }

    @PostMapping("/transfer")
    @Operation(summary = "接口测试-接口管理-接口用例-附件-文件转存")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String transfer(@Validated @RequestBody ApiTransferRequest request) {
        return apiTestCaseService.transfer(request, SessionUtils.getUserId());
    }

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "接口测试-接口管理-接口用例-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @GetMapping("/run/{id}/{reportId}")
    @Operation(summary = "用例执行，获取获取执行结果")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    public TaskRequestDTO run(@PathVariable String id, @PathVariable String reportId) {
        return apiTestCaseService.run(id, reportId);
    }

    @PostMapping("/debug")
    @Operation(summary = "用例调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    public TaskRequestDTO debug(@Validated @RequestBody ApiRunRequest request) {
        return apiTestCaseService.debug(request);
    }
}
