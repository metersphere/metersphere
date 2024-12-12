package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.constants.BugExportColumns;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.BugSyncResult;
import io.metersphere.bug.dto.request.*;
import io.metersphere.bug.dto.response.BugColumnsOptionDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.dto.response.BugDetailDTO;
import io.metersphere.bug.service.BugLogService;
import io.metersphere.bug.service.BugNoticeService;
import io.metersphere.bug.service.BugService;
import io.metersphere.bug.service.BugSyncService;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
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
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * @author song-cc-rock
 */
@Tag(name = "缺陷管理")
@RestController
@RequestMapping("/bug")
public class BugController {

    @Resource
    private BugService bugService;
    @Resource
    private BugSyncService bugSyncService;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @GetMapping("/current-platform/{projectId}")
    @Operation(summary = "缺陷管理-列表-获取当前项目所属平台")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public String getCurrentPlatform(@PathVariable String projectId) {
        return projectApplicationService.getPlatformName(projectId);
    }

    @GetMapping("/header/custom-field/{projectId}")
    @Operation(summary = "缺陷管理-列表-获取表头自定义字段集合")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<TemplateCustomFieldDTO> getHeaderFields(@PathVariable String projectId) {
        return bugService.getHeaderCustomFields(projectId);
    }

    @GetMapping("/header/columns-option/{projectId}")
    @Operation(summary = "缺陷管理-列表-获取表头状态选项")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public BugColumnsOptionDTO getHeaderOption(@PathVariable String projectId) {
        return bugService.getHeaderOption(projectId);
    }

    @PostMapping("/page")
    @Operation(summary = "缺陷管理-列表-分页获取缺陷列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<BugDTO>> page(@Validated @RequestBody BugPageRequest request) {
        request.setUseTrash(false);
        if (request.getBoardCount() || request.getRelatedToPlan() || request.getCreateByMe() || request.getAssignedToMe() || request.getUnresolved()) {
            request.setTodoParam(bugService.buildBugToDoParam(request, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId()));
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, bugService.list(request));
    }

    @FileLimit
    @PostMapping("/add")
    @Operation(summary = "缺陷管理-列表-创建缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request, #files)", msClass = BugLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.BUG_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getNoticeByRequest(#request)", targetClass = BugNoticeService.class)
    public Bug add(@Validated({Created.class}) @RequestPart(value = "request") BugEditRequest request,
                   @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return bugService.addOrUpdate(request, files, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), false);
    }

    @FileLimit
    @PostMapping("/update")
    @Operation(summary = "缺陷管理-列表-编辑缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request, #files)", msClass = BugLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.BUG_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getNoticeByRequest(#request)", targetClass = BugNoticeService.class)
    public Bug update(@Validated({Updated.class}) @RequestPart(value = "request") BugEditRequest request,
                       @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return bugService.addOrUpdate(request, files, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), true);
    }

    @GetMapping("/check-exist/{id}")
    @Operation(summary = "缺陷管理-列表-校验缺陷是否存在")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public boolean check(@PathVariable String id) {
        return bugService.checkExist(id);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "缺陷管理-列表-查看缺陷(详情&&编辑&&复制)")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public BugDetailDTO get(@PathVariable String id) {
        return bugService.get(id, SessionUtils.getUserId(), Objects.requireNonNull(SessionUtils.getUser()).getLanguage());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "缺陷管理-列表-删除缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = BugLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.BUG_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getNoticeById(#id)", targetClass = BugNoticeService.class)
    public void delete(@PathVariable String id) {
        bugService.delete(id, SessionUtils.getUserId());
    }

    @GetMapping("/sync/{projectId}")
    @Operation(summary = "缺陷管理-列表-同步缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public void sync(@PathVariable String projectId) {
        bugSyncService.syncBugs(projectId, SessionUtils.getUserId(), Objects.requireNonNull(SessionUtils.getUser()).getLanguage(), Translator.get("sync_mode.manual"));
    }

    @PostMapping("/sync/all")
    @Operation(summary = "缺陷管理-列表-同步缺陷(区间)")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void syncAll(@RequestBody BugSyncRequest request) {
        bugSyncService.syncAllBugs(request, SessionUtils.getUserId(), Objects.requireNonNull(SessionUtils.getUser()).getLanguage(), Translator.get("sync_mode.manual"));
    }

    @GetMapping("/sync/check/{projectId}")
    @Operation(summary = "缺陷管理-列表-校验缺陷同步状态")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public BugSyncResult checkStatus(@PathVariable String projectId) {
        return bugSyncService.checkSyncStatus(projectId);
    }

    @GetMapping("/export/columns/{projectId}")
    @Operation(summary = "缺陷管理-列表-获取导出字段配置")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public BugExportColumns getExportColumns(@PathVariable String projectId) {
        return bugService.getExportColumns(projectId);
    }

    @PostMapping("/export")
    @Operation(summary = "缺陷管理-列表-批量导出缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_EXPORT)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ResponseEntity<byte[]> export(@Validated @RequestBody BugExportRequest request) throws Exception {
        request.setSort(StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        request.setUseTrash(false);
        return bugService.export(request);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "缺陷管理-列表-批量删除缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDeleteLog(#request)", msClass = BugLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.BUG_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getBatchNoticeByRequest(#request)", targetClass = BugNoticeService.class)
    public void batchDelete(@Validated @RequestBody BugBatchRequest request) {
        request.setUseTrash(false);
        bugService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-update")
    @Operation(summary = "缺陷管理-列表-批量编辑缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @SendNotice(taskType = NoticeConstants.TaskType.BUG_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getBatchNoticeByRequest(#request)", targetClass = BugNoticeService.class)
    public void batchUpdate(@Validated @RequestBody BugBatchUpdateRequest request) {
        request.setUseTrash(false);
        bugService.batchUpdate(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "缺陷管理-列表-拖拽排序")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void editPos(@Validated @RequestBody PosRequest request) {
        bugService.editPos(request);
    }

    @GetMapping("/template/option/{projectId}")
    @Operation(summary = "缺陷管理-详情-获取当前项目模板选项")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<ProjectTemplateOptionDTO> getTemplateOption(@PathVariable String projectId) {
        return projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
    }

    @PostMapping("/template/detail")
    @Operation(summary = "缺陷管理-详情-获取模板详情内容")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public TemplateDTO getTemplateDetail(@RequestBody BugTemplateRequest request) {
        return bugService.getTemplate(request.getId(), request.getProjectId(), request.getFromStatusId(), request.getPlatformBugKey(), request.getShowLocal(), Objects.requireNonNull(SessionUtils.getUser()).getLanguage());
    }

    @GetMapping("/follow/{id}")
    @Operation(summary = "缺陷管理-详情-关注缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public void follow(@PathVariable String id) {
        bugService.follow(id, SessionUtils.getUserId());
    }

    @GetMapping("/unfollow/{id}")
    @Operation(summary = "缺陷管理-详情-取消关注缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public void unfollow(@PathVariable String id) {
        bugService.unfollow(id, SessionUtils.getUserId());
    }
}
