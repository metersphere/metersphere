package io.metersphere.bug.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.constants.BugExportColumns;
import io.metersphere.bug.dto.request.*;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugService;
import io.metersphere.bug.service.BugSyncService;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.dto.sdk.TemplateDTO;
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

    @PostMapping("/page")
    @Operation(summary = "缺陷管理-获取缺陷列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public Pager<List<BugDTO>> page(@Validated @RequestBody BugPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        request.setUseTrash(false);
        return PageUtils.setPageInfo(page, bugService.list(request, SessionUtils.getUserId()));
    }

    @PostMapping("/add")
    @Operation(summary = "缺陷管理-创建缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_ADD)
    public void add(@Validated({Created.class}) @RequestPart(value = "request") BugEditRequest request,
                    @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        bugService.addOrUpdate(request, files, SessionUtils.getUserId(), false);
    }

    @PostMapping("/update")
    @Operation(summary = "缺陷管理-更新缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void update(@Validated({Updated.class}) @RequestPart(value = "request") BugEditRequest request,
                       @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        bugService.addOrUpdate(request, files, SessionUtils.getUserId(), true);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "缺陷管理-删除缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    public void delete(@PathVariable String id) {
        bugService.delete(id);
    }

    @GetMapping("/template/option")
    @Operation(summary = "缺陷管理-获取当前项目缺陷模板选项")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public List<ProjectTemplateOptionDTO> getTemplateOption(@RequestParam(value = "projectId") String projectId) {
        return projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
    }

    @PostMapping("/template/detail")
    @Operation(summary = "缺陷管理-获取模板详情内容")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public TemplateDTO getTemplateDetail(@RequestBody BugTemplateRequest request) {
        return bugService.getTemplate(request.getId(), request.getProjectId(), request.getFromStatusId(), request.getPlatformBugKey());
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "缺陷管理-批量删除缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_DELETE)
    public void batchDelete(@Validated @RequestBody BugBatchRequest request) {
        bugService.batchDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch-update")
    @Operation(summary = "缺陷管理-批量编辑缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void batchUpdate(@Validated @RequestBody BugBatchUpdateRequest request) {
        bugService.batchUpdate(request, SessionUtils.getUserId());
    }

    @GetMapping("/follow/{id}")
    @Operation(summary = "缺陷管理-关注缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void follow(@PathVariable String id) {
        bugService.follow(id, SessionUtils.getUserId());
    }

    @GetMapping("/unfollow/{id}")
    @Operation(summary = "缺陷管理-取消关注缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void unfollow(@PathVariable String id) {
        bugService.unfollow(id, SessionUtils.getUserId());
    }

    @GetMapping("/sync/{projectId}")
    @Operation(summary = "缺陷管理-同步缺陷(开源)")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void sync(@PathVariable String projectId) {
        bugSyncService.syncBugs(projectId);
    }

    @PostMapping("/sync/all")
    @Operation(summary = "缺陷管理-同步缺陷(全量)")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    public void syncAll(@RequestBody BugSyncRequest request) {
        bugSyncService.syncAllBugs(request);
    }

    @GetMapping("/export/columns/{projectId}")
    @Operation(summary = "缺陷管理-获取导出字段配置")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_EXPORT)
    public BugExportColumns getExportColumns(@PathVariable String projectId) {
        return bugService.getExportColumns(projectId);
    }

    @PostMapping("/export")
    @Operation(summary = "缺陷管理-批量导出缺陷")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_EXPORT)
    public ResponseEntity<byte[]> export(@Validated @RequestBody BugExportRequest request) throws Exception {
        return bugService.export(request, SessionUtils.getUserId());
    }
}
