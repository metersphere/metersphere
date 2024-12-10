package io.metersphere.api.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.scenario.ApiFileCopyRequest;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioStepMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.*;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
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
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author lan
 */
@RestController
@RequestMapping(value = "/api/definition")
@Tag(name = "接口测试-接口管理-接口定义")
public class ApiDefinitionController {

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiScenarioStepMapper extApiScenarioStepMapper;

    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiDefinitionImportService apiDefinitionImportService;
    @Resource
    private ApiDefinitionExportService apiDefinitionExportService;
    @Resource
    private ApiScenarioService apiScenarioService;

    /*
     接口覆盖率
        业务注释，误删
        * 一个接口如果被跨项目的场景给关联了，算不算覆盖？  不算
        * 自定义请求， 不管它有多少个“/"有多少子域 ， 跟接口定义匹配的时候就用末端匹配法。
            · 例如：https://www.tapd.cn/tapd_fe/my/work?dialog_preview_id=abcdefg
                ·/work能匹配的上
                ·/my/work能匹配的上
                ·/my 不可以
                ·/my/{something}可以匹配的上
                ·/my/{something}/{other-thing}不可以
        * 剩下的基本上就跟V2一样了. 有用例 or  被场景引用/复制 or 被自定义给命中了  就算覆盖。 且自定义请求可以命中多个接口定义，比如上一点
     */
    @GetMapping("/rage/{projectId}")
    @Operation(summary = "接口测试-接口管理-接口列表(deleted 状态为 1 时为回收站数据)")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public ApiCoverageDTO rage(@PathVariable String projectId) {
        // 筛选出所有 API 的 ID 和 HTTP 类型的 API
        List<ApiDefinition> apiDefinitions = extApiDefinitionMapper.selectBaseInfoByProjectId(projectId, null, null);
        List<String> apiAllIds = apiDefinitions.stream().map(ApiDefinition::getId).toList();
        List<ApiDefinition> httpApiList = apiDefinitions.stream()
                .filter(api -> StringUtils.equalsIgnoreCase(api.getProtocol(), "http"))
                .toList();

        // 获取 API 定义、测试用例 ID 和场景步骤中的 API ID
        List<String> apiDefinitionIdFromCase = extApiTestCaseMapper.selectApiId(projectId);
        List<String> apiInScenarioStep = new ArrayList<>(extApiScenarioStepMapper.selectResourceId(projectId, ApiScenarioStepType.API.name()));
        List<String> apiCaseIdInStep = extApiScenarioStepMapper.selectResourceId(projectId, ApiScenarioStepType.API_CASE.name());

        // 如果有场景步骤中的 API 用例 ID，追加相关 API ID
        if (CollectionUtils.isNotEmpty(apiCaseIdInStep)) {
            List<String> apiCaseIdInScenarioStep = extApiTestCaseMapper.selectApiIdByCaseId(apiCaseIdInStep, null, null);
            apiInScenarioStep.addAll(apiCaseIdInScenarioStep);
        }

        // 获取自定义步骤中的 API ID 并合并
        List<String> apiInStepList = new ArrayList<>(apiScenarioService.selectApiIdInCustomRequest(projectId, httpApiList));
        apiInStepList.addAll(apiInScenarioStep);

        // 构建结果 DTO
        return new ApiCoverageDTO(apiAllIds, apiDefinitionIdFromCase, apiInStepList);
    }

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

    @PostMapping("/file/copy")
    @Operation(summary = "接口测试-接口管理-复制接口时，复制文件")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_UPDATE, PermissionConstants.PROJECT_API_DEFINITION_ADD},
            logical = Logical.OR)
    public Map<String, String> copyFile(@Validated @RequestBody ApiFileCopyRequest request) {
        return apiDefinitionService.copyFile(request);
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
        apiDefinitionService.initApiSelectIds(request);
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

    @FileLimit
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
        apiDefinitionImportService.apiDefinitionImport(file, request, SessionUtils.getCurrentProjectId());
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

    @PostMapping("/json-schema/preview")
    @Operation(summary = "接口测试-接口管理-接口-json-schema-预览")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public String preview(@RequestBody JsonSchemaItem jsonSchemaItem) {
        return apiDefinitionService.preview(jsonSchemaItem);
    }

    @PostMapping("/json-schema/auto-generate")
    @Operation(summary = "接口测试-接口管理-接口-json-schema-自动生成测试数据")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public String jsonSchemaAutoGenerate(@RequestBody JsonSchemaItem jsonSchemaItem) {
        return apiDefinitionService.jsonSchemaAutoGenerate(jsonSchemaItem);
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

    @PostMapping(value = "/export/{type}")
    @Operation(summary = "接口测试-接口管理-导出接口定义")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_EXPORT)
    public String export(@RequestBody ApiDefinitionBatchExportRequest request, @PathVariable String type) {
        return apiDefinitionExportService.exportApiDefinition(request, type, SessionUtils.getUserId());
    }

    @GetMapping("/stop/{taskId}")
    @Operation(summary = "接口测试-接口管理-导出-停止导出")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_EXPORT)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void caseStopExport(@PathVariable String taskId) {
        apiDefinitionExportService.stopExport(taskId, SessionUtils.getUserId());
    }

    @GetMapping(value = "/download/file/{projectId}/{fileId}")
    @Operation(summary = "接口测试-接口管理-下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_EXPORT)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void downloadImgById(@PathVariable String projectId, @PathVariable String fileId, HttpServletResponse httpServletResponse) {
        apiDefinitionExportService.downloadFile(projectId, fileId, SessionUtils.getUserId(), httpServletResponse);
    }

}
