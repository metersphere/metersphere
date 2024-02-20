package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.service.definition.ApiDefinitionLogService;
import io.metersphere.api.service.definition.ApiDefinitionNoticeService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
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
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * @author lan
 */
@RestController
@RequestMapping(value = "/api/definition")
@Tag(name = "接口测试-接口管理-接口定义")
public class ApiDefinitionController {
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping(value = "/add")
    @Operation(summary = "接口测试-接口管理-添加接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getApiDTO(#request)", targetClass = ApiDefinitionNoticeService.class)
    public ApiDefinition add(@Validated @RequestBody ApiDefinitionAddRequest request) {
        return apiDefinitionService.create(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "接口测试-接口管理-更新接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getUpdateApiDTO(#request)", targetClass = ApiDefinitionNoticeService.class)
    public ApiDefinition update(@Validated @RequestBody ApiDefinitionUpdateRequest request) {
        return apiDefinitionService.update(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-update")
    @Operation(summary = "接口测试-接口管理-批量更新接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getBatchEditApiDTO(#request)", targetClass = ApiDefinitionNoticeService.class)
    public void batchUpdate(@Validated @RequestBody ApiDefinitionBatchUpdateRequest request) {
        apiDefinitionService.batchUpdate(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "接口测试-接口管理-删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.delLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition")
    public void delete(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.delete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-del")
    @Operation(summary = "接口测试-接口管理-批量删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getBatchEditApiDTO(#request)", targetClass = ApiDefinitionNoticeService.class)
    public void batchDelete(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/copy")
    @Operation(summary = "接口测试-接口管理-复制接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.copyLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public ApiDefinition copy(@Validated @RequestBody ApiDefinitionCopyRequest request) {
        return apiDefinitionService.copy(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-move")
    @Operation(summary = "接口测试-接口管理-批量移动接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchMove(@Validated @RequestBody ApiDefinitionBatchMoveRequest request) {
        apiDefinitionService.batchMove(request, SessionUtils.getUserId());
    }

    @GetMapping("/version/{id}")
    @Operation(summary = "接口测试-接口管理-版本信息(接口是否存在多版本)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition")
    public List<ApiDefinitionVersionDTO> getApiDefinitionVersion(@PathVariable @NotBlank(message = "{api_definition.id.not_blank}") String id) {
        return apiDefinitionService.getApiDefinitionVersion(id);
    }

    @GetMapping(value = "/get-detail/{id}")
    @Operation(summary = "接口测试-接口管理-获取接口详情")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition")
    public ApiDefinitionDTO get(@PathVariable String id) {
        return apiDefinitionService.get(id, SessionUtils.getUserId());
    }

    @GetMapping("/follow/{id}")
    @Operation(summary = "接口测试-接口管理-关注/取消关注用例")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.followLog(#id)", msClass = ApiDefinitionLogService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition")
    public void follow(@PathVariable String id) {
        apiDefinitionService.follow(id, SessionUtils.getUserId());
    }

    @PostMapping("/page")
    @Operation(summary = "接口测试-接口管理-接口列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiDefinitionDTO>> getPage(@Validated @RequestBody ApiDefinitionPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getApiDefinitionPage(request, SessionUtils.getUserId()));
    }

    @PostMapping(value = "/recover")
    @Operation(summary = "接口测试-接口管理-恢复回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_RECOVER)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition")
    public void recover(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.recover(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/trash-del")
    @Operation(summary = "接口测试-接口管理-删除回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.trashDelLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition")
    public void trashDel(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.trashDel(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-recover")
    @Operation(summary = "接口测试-接口管理-批量从回收站恢复接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_RECOVER)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchRecover(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchRecover(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-trash-del")
    @Operation(summary = "接口测试-接口管理-批量从回收站删除接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchTrashDel(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchTrashDel(request, SessionUtils.getUserId());
    }

    @PostMapping("/page-doc")
    @Operation(summary = "接口测试-接口管理-接口文档列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<ApiDefinitionDTO>> getDocPage(@Validated @RequestBody ApiDefinitionPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getDocPage(request, SessionUtils.getUserId()));
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "上传接口定义所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_API_DEFINITION_ADD, PermissionConstants.PROJECT_API_DEFINITION_UPDATE})
    public String uploadTempFile(@RequestParam("file") MultipartFile file) {
        return apiDefinitionService.uploadTempFile(file);
    }

    @PostMapping("/doc")
    @Operation(summary = "接口测试-接口管理-接口文档列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiDefinitionDocDTO getDocInfo(@Validated @RequestBody ApiDefinitionDocRequest request) {
        return apiDefinitionService.getDocInfo(request);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @Operation(summary = "接口测试-接口管理-导入接口定义")
    public void testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ImportRequest request) {
        apiDefinitionService.apiTestImport(file, request, SessionUtils.getUser(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/operation-history")
    @Operation(summary = "接口测试-接口管理-接口变更历史")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getSourceId()", resourceType = "api_definition")
    public Pager<List<OperationHistoryDTO>> operationHistoryList(@Validated @RequestBody OperationHistoryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.list(request));
    }

    @PostMapping("/operation-history/recover")
    @Operation(summary = "接口测试-接口管理-接口变更历史恢复")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "operation_history")
    public void operationHistoryRecover(@Validated @RequestBody OperationHistoryVersionRequest request) {
        apiDefinitionService.recoverOperationHistory(request);
    }

    @PostMapping("/operation-history/save")
    @Operation(summary = "接口测试-接口管理-另存变更历史为指定版本")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "operation_history")
    public void saveOperationHistory(@Validated @RequestBody OperationHistoryVersionRequest request) {
        apiDefinitionService.saveOperationHistory(request);
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "接口测试-接口管理-接口-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.moveLog(#request.getTargetId())", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getTargetId()", resourceType = "api_definition")
    public void editPos(@Validated @RequestBody ApiEditPosRequest request) {
        apiDefinitionService.editPos(request, SessionUtils.getUserId());
    }
}
