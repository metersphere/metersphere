package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.dto.ExportTaskDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.dto.response.FunctionalCaseImportResponse;
import io.metersphere.functional.excel.domain.FunctionalCaseExportColumns;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.*;
import io.metersphere.project.dto.CustomFieldOptions;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.sdk.TemplateDTO;
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
import jakarta.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
@Tag(name = "用例管理-功能用例")
@RestController
@RequestMapping("/functional/case")
public class FunctionalCaseController {

    @Resource
    private FunctionalCaseService functionalCaseService;

    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private FunctionalCaseFileService functionalCaseFileService;
    @Resource
    private FunctionalCaseXmindService functionalCaseXmindService;

    //TODO 获取模板列表(多模板功能暂时不做)


    @GetMapping("/default/template/field/{projectId}")
    @Operation(summary = "用例管理-功能用例-获取默认模板自定义字段")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public TemplateDTO getDefaultTemplateField(@PathVariable String projectId) {
        return projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.FUNCTIONAL.name());
    }


    @FileLimit
    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例-新增用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getAddMainFunctionalCaseDTO(#request,  #request.customFields)", targetClass = FunctionalCaseNoticeService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalCase addFunctionalCase(@Validated @RequestPart("request") FunctionalCaseAddRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getUserId();
        String organizationId = SessionUtils.getCurrentOrganizationId();
        return functionalCaseService.addFunctionalCase(request, files, userId, organizationId);
    }


    @GetMapping("/detail/{id}")
    @Operation(summary = "用例管理-功能用例-查看用例详情")
    @RequiresPermissions(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.CASE_REVIEW_READ}, logical = Logical.OR)
    @CheckOwner(resourceId = "#id", resourceType = "functional_case")
    public FunctionalCaseDetailDTO getFunctionalCaseDetail(@PathVariable String id) {
        String userId = SessionUtils.getUserId();
        return functionalCaseService.getFunctionalCaseDetail(id, userId, true);
    }

    @FileLimit
    @PostMapping("/update")
    @Operation(summary = "用例管理-功能用例-更新用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateFunctionalCaseLog(#request, #files)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainFunctionalCaseDTO(#request,  #request.customFields)", targetClass = FunctionalCaseNoticeService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "functional_case")
    public FunctionalCase updateFunctionalCase(@Validated @RequestPart("request") FunctionalCaseEditRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String userId = SessionUtils.getUserId();
        return functionalCaseService.updateFunctionalCase(request, files, userId);
    }


    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-功能用例-关注/取消关注用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getFunctionalCaseId()", resourceType = "functional_case")
    public void editFollower(@Validated @RequestBody FunctionalCaseFollowerRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.editFollower(request.getFunctionalCaseId(), userId);
    }


    @GetMapping("/version/{id}")
    @Operation(summary = "用例管理-功能用例-版本信息(用例是否存在多版本)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#id", resourceType = "functional_case")
    public List<FunctionalCaseVersionDTO> getVersion(@PathVariable @NotBlank(message = "{functional_case.id.not_blank}") String id) {
        return functionalCaseService.getFunctionalCaseVersion(id);
    }


    @PostMapping("/delete")
    @Operation(summary = "用例管理-功能用例-删除用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "functional_case")
    public void deleteFunctionalCase(@Validated @RequestBody FunctionalCaseDeleteRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.deleteFunctionalCase(request, userId);
    }


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-用例列表查询")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FunctionalCasePageDTO>> getFunctionalCasePage(@Validated @RequestBody FunctionalCasePageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, functionalCaseService.getFunctionalCasePage(request, false, true));
    }

    @PostMapping("/module/count")
    @Operation(summary = "用例管理-功能用例-统计模块数量")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody FunctionalCasePageRequest request) {
        return functionalCaseService.moduleCount(request, false);
    }

    @PostMapping("/batch/delete-to-gc")
    @Operation(summary = "用例管理-功能用例-批量删除用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDeleteFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchDeleteFunctionalCaseToGc(@Validated @RequestBody FunctionalCaseBatchRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.batchDeleteFunctionalCaseToGc(request, userId);
    }

    @GetMapping("/custom/field/{projectId}")
    @Operation(summary = "用例管理-功能用例-获取表头自定义字段(高级搜索中的自定义字段)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<CustomFieldOptions> getTableCustomField(@PathVariable String projectId) {
        return projectTemplateService.getTableCustomField(projectId, TemplateScene.FUNCTIONAL.name());
    }

    @PostMapping("/batch/move")
    @Operation(summary = "用例管理-功能用例-批量移动用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchMoveFunctionalCase(@Validated @RequestBody FunctionalCaseBatchMoveRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.batchMoveFunctionalCase(request, userId);
    }


    @PostMapping("/batch/copy")
    @Operation(summary = "用例管理-功能用例-批量复制用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchCopyFunctionalCase(@Validated @RequestBody FunctionalCaseBatchMoveRequest request) {
        String userId = SessionUtils.getUserId();
        String organizationId = SessionUtils.getCurrentOrganizationId();
        functionalCaseService.batchCopyFunctionalCase(request, userId, organizationId);
    }


    @PostMapping("/batch/edit")
    @Operation(summary = "用例管理-功能用例-批量编辑用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchEditFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchEditFunctionalCase(@Validated @RequestBody FunctionalCaseBatchEditRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseService.batchEditFunctionalCase(request, userId);
    }


    @PostMapping("edit/pos")
    @Operation(summary = "用例管理-功能用例-拖拽排序")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTargetId()", resourceType = "functional_case")
    public void editPos(@Validated @RequestBody PosRequest request) {
        functionalCaseService.editPos(request);
    }


    @GetMapping("/download/excel/template/{projectId}")
    @Operation(summary = "用例管理-功能用例-excel导入-下载模板")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void testCaseTemplateExport(@PathVariable String projectId, HttpServletResponse response) {
        functionalCaseFileService.downloadExcelTemplate(projectId, response);
    }

    @PostMapping("/pre-check/excel")
    @Operation(summary = "用例管理-功能用例-excel导入检查")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_IMPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalCaseImportResponse preCheckExcel(@RequestPart("request") FunctionalCaseImportRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return functionalCaseFileService.preCheckExcel(request, file);
    }

    @PostMapping("/pre-check/xmind")
    @Operation(summary = "用例管理-功能用例-xmind导入检查")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_IMPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalCaseImportResponse preCheckXMind(@RequestPart("request") FunctionalCaseImportRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        SessionUser user = SessionUtils.getUser();
        return functionalCaseFileService.preCheckXMind(request, user, file);
    }


    @PostMapping("/import/excel")
    @Operation(summary = "用例管理-功能用例-excel导入")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_IMPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalCaseImportResponse importExcel(@RequestPart("request") FunctionalCaseImportRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        SessionUser user = SessionUtils.getUser();
        return functionalCaseFileService.importExcel(request, user, file);
    }

    @PostMapping("/import/xmind")
    @Operation(summary = "用例管理-功能用例-xmind导入")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_IMPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalCaseImportResponse importXMind(@RequestPart("request") FunctionalCaseImportRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        SessionUser user = SessionUtils.getUser();
        return functionalCaseFileService.importXMind(request, user, file);
    }

    @PostMapping("/operation-history")
    @Operation(summary = "用例管理-功能用例-变更历史")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getSourceId()", resourceType = "functional_case")
    public Pager<List<OperationHistoryDTO>> operationHistoryList(@Validated @RequestBody OperationHistoryRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, functionalCaseService.operationHistoryList(request));
    }


    @PostMapping("/export/excel")
    @Operation(summary = "用例管理-功能用例-excel导出")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_EXPORT)
    public String testCaseExport(@Validated @RequestBody FunctionalCaseExportRequest request) {
        return functionalCaseFileService.export(SessionUtils.getUserId(), request, SessionUtils.getCurrentOrganizationId());
    }

    @GetMapping("/stop/{taskId}")
    @Operation(summary = "用例管理-功能用例-导出-停止导出")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_EXPORT)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void caseStopExport(@PathVariable String taskId) {
        functionalCaseFileService.stopExport(taskId, SessionUtils.getUserId());
    }

    @GetMapping("/download/xmind/template/{projectId}")
    @Operation(summary = "用例管理-功能用例-xmind导入-下载模板")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void xmindTemplateExport(@PathVariable String projectId, HttpServletResponse response) {
        functionalCaseXmindService.downloadXmindTemplate(projectId, response);
    }

    @GetMapping("/export/columns/{projectId}")
    @Operation(summary = "用例管理-获取导出字段配置")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public FunctionalCaseExportColumns getExportColumns(@PathVariable String projectId) {
        return functionalCaseFileService.getExportColumns(projectId);
    }


    @GetMapping(value = "/download/file/{projectId}/{fileId}")
    @Operation(summary = "用例管理-功能用例-下载文件")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_EXPORT)
    public ResponseEntity<byte[]> downloadImgById(@PathVariable String projectId, @PathVariable String fileId) {
        return functionalCaseFileService.downloadFile(projectId, fileId, SessionUtils.getUserId());
    }


    @PostMapping("/export/xmind")
    @Operation(summary = "用例管理-功能用例-xmind导出")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_EXPORT)
    public String caseExportXmind(@Validated @RequestBody FunctionalCaseExportRequest request) {
        return functionalCaseXmindService.exportFunctionalCaseXmind(request, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId());
    }

    @GetMapping(value = "/check/export-task")
    @Operation(summary = "用例管理-功能用例-导出任务校验")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_EXPORT)
    public ExportTaskDTO checkExportTask() {
        return functionalCaseFileService.checkExportTask(SessionUtils.getCurrentProjectId(), SessionUtils.getUserId());
    }
}
