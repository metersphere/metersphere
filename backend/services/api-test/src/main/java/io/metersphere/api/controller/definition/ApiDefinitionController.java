package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.ApiDefinitionImportService;
import io.metersphere.api.service.definition.ApiDefinitionLogService;
import io.metersphere.api.service.definition.ApiDefinitionNoticeService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.utils.JsonSchemaBuilder;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
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
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiDefinitionImportService apiDefinitionImportService;

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
    public void batchUpdate(@Validated @RequestBody ApiDefinitionBatchUpdateRequest request) {
        apiDefinitionService.batchUpdate(request, SessionUtils.getUserId());
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
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : request.getDeleted() ? "delete_time desc, id desc" : "pos desc, id desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getApiDefinitionPage(request, SessionUtils.getUserId()));
    }

    @GetMapping("/delete-to-gc/{id}")
    @Operation(summary = "接口测试-接口管理-删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.moveToGcLog(#id)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition")
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getDeleteApiDTO(#id)", targetClass = ApiDefinitionNoticeService.class)
    public void deleteToGc(@PathVariable String id, @RequestParam(required = false) boolean deleteAllVersion) {
        apiDefinitionService.deleteToGc(id, deleteAllVersion, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch/delete-to-gc")
    @Operation(summary = "接口测试-接口管理-批量删除接口定义到回收站")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchDeleteToGc(@Validated @RequestBody ApiDefinitionBatchDeleteRequest request) {
        apiDefinitionService.batchDeleteToGc(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口管理-删除回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition")
    public void delete(@PathVariable String id) {
        apiDefinitionService.delete(id, SessionUtils.getUserId());
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "接口测试-接口管理-批量从回收站删除接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchDelete(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/recover")
    @Operation(summary = "接口测试-接口管理-恢复回收站接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#request)", msClass = ApiDefinitionLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "api_definition")
    public void recover(@Validated @RequestBody ApiDefinitionDeleteRequest request) {
        apiDefinitionService.recover(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-recover")
    @Operation(summary = "接口测试-接口管理-批量从回收站恢复接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "api_definition")
    public void batchRecover(@Validated @RequestBody ApiDefinitionBatchRequest request) {
        apiDefinitionService.batchRecover(request, SessionUtils.getUserId());
    }

    @PostMapping("/page-doc")
    @Operation(summary = "接口测试-接口管理-接口文档列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_READ)
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
        return apiFileResourceService.uploadTempFile(file);
    }

    @PostMapping("/doc")
    @Operation(summary = "接口测试-接口管理-接口文档列表")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ApiDefinitionDocDTO getDocInfo(@Validated @RequestBody ApiDefinitionDocRequest request) {
        return apiDefinitionService.getDocInfo(request);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_IMPORT)
    @Operation(summary = "接口测试-接口管理-导入接口定义")
    public void testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ImportRequest request) {
        request.setUserId(SessionUtils.getUserId());
        apiDefinitionImportService.apiTestImport(file, request, SessionUtils.getCurrentProjectId());
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

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "接口测试-接口管理-接口-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/transfer")
    @Operation(summary = "接口测试-接口管理-接口-附件-文件转存")
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    public String transfer(@Validated @RequestBody ApiTransferRequest request) {
        String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(request.getProjectId(), request.getSourceId());
        return apiFileResourceService.transfer(request, SessionUtils.getUserId(), apiDefinitionDir);
    }

    @PostMapping("/preview")
    @Operation(summary = "接口测试-接口管理-接口-json-schema-预览")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public String preview(@RequestBody JsonSchemaItem jsonSchemaItem) {
        return JsonSchemaBuilder.preview(JSON.toJSONString(jsonSchemaItem));
    }

    @PostMapping("/debug")
    @Operation(summary = "接口调试")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_EXECUTE)
    public TaskRequestDTO debug(@Validated @RequestBody ApiDefinitionRunRequest request) {
        return apiDefinitionService.debug(request);
    }

    @PostMapping("/get-reference")
    @Operation(summary = "接口测试-接口管理-引用关系")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.getResourceId()", resourceType = "api_definition")
    public Pager<List<ReferenceDTO>> getReference(@Validated @RequestBody ReferenceRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "id desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.getReference(request));
    }
}
