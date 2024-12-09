package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.ApiCaseCompareData;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.scenario.ApiFileCopyRequest;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.*;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/api/case")
@Tag(name = "接口测试-接口管理-接口用例")
public class ApiTestCaseController {
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiTestCaseRunService apiTestCaseRunService;
    @Resource
    private ApiTestCaseRecoverService apiTestCaseRecoverService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private ApiTestCaseBatchRunService apiTestCaseBatchRunService;
    @Resource
    private ApiFileResourceService apiFileResourceService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-接口用例-新增")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiTestCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_CREATE, target = "#targetClass.addCaseDto(#request)", targetClass = ApiTestCaseNoticeService.class)
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
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public void recover(@PathVariable String id) {
        ApiTestCaseBatchRequest request = new ApiTestCaseBatchRequest();
        List<String> ids = new ArrayList<>();
        ids.add(id);
        request.setSelectIds(ids);
        request.setProjectId(SessionUtils.getCurrentProjectId());
        request.setProtocols(List.of(ApiConstants.HTTP_PROTOCOL));
        apiTestCaseRecoverService.batchRecover(request, SessionUtils.getUserId());
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

    @PostMapping("/file/copy")
    @Operation(summary = "接口测试-接口管理-复制用例时，复制文件")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD},
            logical = Logical.OR)
    public Map<String, String> copyFile(@Validated @RequestBody ApiFileCopyRequest request) {
        return apiTestCaseService.copyFile(request);
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
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiTestCaseDTO>> page(@Validated @RequestBody ApiTestCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "pos desc, id desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.page(request, false, true, null));
    }

    @PostMapping(value = "/statistics")
    @Operation(summary = "接口测试-接口管理-接口用例-统计")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    public List<ApiTestCaseDTO> calculate(@RequestBody List<String> ids) {
        return apiTestCaseService.calculate(ids);
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
    public void deleteToGcByParam(@RequestBody ApiTestCaseBatchRequest request) {
        apiTestCaseService.batchMoveGc(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/edit")
    @Operation(summary = "接口测试-接口管理-接口用例-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
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

    @PostMapping("/batch/api-change/sync")
    @Operation(summary = "接口测试-接口管理-接口用例-批量编辑")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    public void batchSyncApiChange(@Validated @RequestBody ApiCaseBatchSyncRequest request) {
        apiTestCaseService.batchSyncApiChange(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/trash/page")
    @Operation(summary = "接口测试-接口管理-接口用例-回收站-分页查询")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiTestCaseDTO>> pageTrash(@Validated @RequestBody ApiTestCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "delete_time desc, id desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.page(request, true, true, null));
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "接口测试-接口管理-接口用例-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @CheckOwner(resourceId = "#request.getTargetId()", resourceType = "api_test_case")
    public void editPos(@Validated @RequestBody PosRequest request) {
        apiTestCaseService.moveNode(request);
    }

    @FileLimit
    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口调试所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }

    @PostMapping("/execute/page")
    @Operation(summary = "接口测试-接口管理-接口用例-获取执行历史")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_test_case")
    public Pager<List<ExecuteReportDTO>> getExecuteList(@Validated @RequestBody ExecutePageRequest request) {
        String sort = StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "eti.create_time desc";
        if (StringUtils.isNotBlank(sort)) {
            sort = sort.replace("start_time", "eti.create_time");
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), sort);
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
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String transfer(@Validated @RequestBody ApiTransferRequest request) {
        String apiCaseDir = DefaultRepositoryDir.getApiCaseDir(request.getProjectId(), request.getSourceId());
        return apiFileResourceService.transfer(request, SessionUtils.getUserId(), apiCaseDir);
    }

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "接口测试-接口管理-接口用例-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/debug")
    @Operation(summary = "用例调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    public TaskRequestDTO debug(@Validated @RequestBody ApiCaseRunRequest request) {
        return apiTestCaseRunService.debug(request, SessionUtils.getUserId());
    }

    @GetMapping("/run/{id}")
    @Operation(summary = "用例执行, 传ID执行")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public TaskRequestDTO run(@PathVariable String id,
                              @Schema(description = "报告ID，传了可以实时获取结果，不传则不支持实时获取")
                              @RequestParam(required = false) String reportId) {
        return apiTestCaseRunService.run(id, reportId, SessionUtils.getUserId());
    }

    @PostMapping("/run")
    @Operation(summary = "用例执行，传请求详情执行")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    public TaskRequestDTO run(@Validated @RequestBody ApiCaseRunRequest request) {
        return apiTestCaseRunService.run(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/run")
    @Operation(summary = "批量执行")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_test_case")
    public void batchRun(@Validated @RequestBody ApiTestCaseBatchRunRequest request) {
        apiTestCaseBatchRunService.batchRun(request, SessionUtils.getUserId());
    }

    @PostMapping("/get-reference")
    @Operation(summary = "接口测试-接口管理-接口用例-引用关系")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#request.getResourceId()", resourceType = "api_test_case")
    public Pager<List<ReferenceDTO>> getReference(@Validated @RequestBody ReferenceRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "id desc");
        return PageUtils.setPageInfo(page, apiTestCaseService.getReference(request));
    }

    @GetMapping("/api-change/clear/{id}")
    @Operation(summary = "清除接口参数变更标识")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.clearApiChangeLog(#id)", msClass = ApiTestCaseLogService.class)
    public void clearApiChange(@PathVariable String id) {
        apiTestCaseService.clearApiChange(id);
    }

    @GetMapping("/api-change/ignore/{id}")
    @Operation(summary = "忽略接口变更提示")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE})
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.ignoreApiChange(#id)", msClass = ApiTestCaseLogService.class)
    public void ignoreApiChange(@PathVariable String id, @RequestParam(name = "ignore") boolean ignore) {
        apiTestCaseService.ignoreApiChange(id, ignore);
    }

    @PostMapping("/api-change/sync")
    @Operation(summary = "获取同步后的用例详情")
    @RequiresPermissions(value = PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_test_case")
    public AbstractMsTestElement syncApiChange(@Validated @RequestBody ApiCaseSyncRequest request) {
        return apiTestCaseService.syncApiChange(request);
    }

    @GetMapping("/api/compare/{id}")
    @Operation(summary = "与接口定义对比")
    @RequiresPermissions(value = PermissionConstants.PROJECT_API_DEFINITION_CASE_READ)
    @CheckOwner(resourceId = "#id", resourceType = "api_test_case")
    public ApiCaseCompareData getApiCompareData(@PathVariable String id) {
        return apiTestCaseService.getApiCompareData(id);
    }
}
