package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.IssueSyncCheckResult;
import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.IssuesStatusCountDao;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.platform.domain.SelectOption;
import io.metersphere.request.PlatformOptionRequest;
import io.metersphere.request.issues.IssueExportRequest;
import io.metersphere.request.issues.IssueImportRequest;
import io.metersphere.request.issues.PlatformIssueTypeRequest;
import io.metersphere.request.testcase.AuthUserIssueRequest;
import io.metersphere.request.testcase.IssuesCountRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.IssuesService;
import io.metersphere.service.IssuesSyncService;
import io.metersphere.service.PlatformPluginService;
import io.metersphere.xpack.track.dto.*;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

@RequestMapping("issues")
@RestController
public class IssuesController {

    @Resource
    private IssuesService issuesService;
    @Resource
    private IssuesSyncService issuesSyncService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private PlatformPluginService platformPluginService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public Pager<List<IssuesDao>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody IssuesRequest request) {
        issuesService.setFilterIds(request);
        if (request.getThisWeekUnClosedTestPlanIssue() && CollectionUtils.isEmpty(request.getFilterIds())) {
            Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
            return PageUtils.setPageInfo(page, Collections.EMPTY_LIST);
        } else {
            Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
            return PageUtils.setPageInfo(page, issuesService.list(request));
        }
    }

    @PostMapping("/dashboard/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public Pager<List<IssuesDao>> listByWorkspaceId(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody IssuesRequest request) {
        Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, issuesService.listByWorkspaceId(request));
    }

    @PostMapping("/list/relate/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public Pager<List<IssuesDao>> relateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody IssuesRequest request) {
        Page<List<Issues>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, issuesService.relateList(request));
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#issuesRequest)", msClass = IssuesService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.DEFECT_TASK, event = NoticeConstants.Event.CREATE, subject = "缺陷通知")
    public IssuesWithBLOBs addIssues(@RequestPart(value = "request") IssuesUpdateRequest issuesRequest, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return issuesService.addIssues(issuesRequest, files);
    }

    @PostMapping(value = "/update")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#issuesRequest.id)", content = "#msClass.getLogDetails(#issuesRequest.id)", msClass = IssuesService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.DEFECT_TASK, event = NoticeConstants.Event.UPDATE, subject = "缺陷通知")
    public IssuesWithBLOBs updateIssues(@RequestPart(value = "request") IssuesUpdateRequest issuesRequest) {
        return issuesService.updateIssues(issuesRequest);
    }

    @GetMapping("/get/case/{refType}/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public List<IssuesDao> getIssues(@PathVariable String refType, @PathVariable String id) {
        return issuesService.getIssues(id, refType);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public IssuesWithBLOBs getIssue(@PathVariable String id) {
        return issuesService.getIssue(id);
    }

    @GetMapping("/plan/get/{planId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public List<IssuesDao> getIssuesByPlanId(@PathVariable String planId) {
        return issuesService.getIssuesByPlanId(planId);
    }

    @GetMapping("/auth/{workspaceId}/{platform}")
    public void testAuth(@PathVariable String workspaceId, @PathVariable String platform) {
        issuesService.testAuth(workspaceId, platform);
    }

    @PostMapping("/user/auth")
    public void userAuth(@RequestBody AuthUserIssueRequest authUserIssueRequest) {
        issuesService.userAuth(authUserIssueRequest);
    }

    @GetMapping("/close/{id}")
    public void closeLocalIssue(@PathVariable String id) {
        issuesService.closeLocalIssue(id);
    }

    @PostMapping("/delete/relate")
    @MsRequestLog(module = OperLogModule.TRACK_BUG)
    public void deleteRelate(@RequestBody IssuesRequest request) {
        issuesService.deleteIssueRelate(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = IssuesService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.DEFECT_TASK, target = "#targetClass.getIssue(#id)", targetClass = IssuesService.class, event = NoticeConstants.Event.DELETE, subject = "缺陷通知")
    public void delete(@PathVariable String id) {
        issuesService.delete(id);
    }

    @PostMapping("/batchDelete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.DELETE, msClass = IssuesService.class)
    public void batchDelete(@RequestBody IssuesUpdateRequest request) {
        issuesService.batchDelete(request);
    }

    @PostMapping("/tapd/user")
    public List<PlatformUser> getTapdUsers(@RequestBody IssuesRequest request) {
        return issuesService.getTapdProjectUsers(request);
    }

    @GetMapping("/tapd/current_owner/{id}")
    public List<String> getTapdIssueCurrentOwner(@PathVariable String id) {
        return issuesService.getTapdIssueCurrentOwner(id);
    }

    @GetMapping("/sync/{projectId}")
    public void syncThirdPartyIssues(@PathVariable String projectId) {
        issuesSyncService.syncIssues(projectId);
    }

    @PostMapping("/sync/all")
    public void syncThirdPartyAllIssues(@RequestBody IssueSyncRequest request) {
        issuesSyncService.syncAllIssues(request);
    }

    @GetMapping("/sync/check/{projectId}")
    public IssueSyncCheckResult checkSync(@PathVariable String projectId) {
        return issuesService.checkSync(projectId);
    }

    @PostMapping("/change/status")
    public void changeStatus(@RequestBody IssuesRequest request) {
        issuesService.changeStatus(request);
    }

    @PostMapping("/status/count")
    public List<IssuesStatusCountDao> getCountByStatus(@RequestBody IssuesCountRequest request) {
        return issuesService.getCountByStatus(request);
    }

    @GetMapping("/follow/{issueId}")
    public List<String> getFollows(@PathVariable String issueId) {
        return issuesService.getFollows(issueId);
    }

    @PostMapping("/up/follows/{issueId}")
    @MsRequestLog(module = OperLogModule.TRACK_BUG)
    public void saveFollows(@PathVariable String issueId,@RequestBody List<String> follows) {
        issuesService.saveFollows(issueId,follows);
    }

    @GetMapping("/thirdpart/template/{projectId}")
    public IssueTemplateDao getThirdPartTemplate(@PathVariable String projectId) {
        return issuesService.getThirdPartTemplate(projectId);
    }

    @GetMapping("/plugin/custom/fields/{projectId}")
    public List<CustomFieldDao> getPluginCustomFields(@PathVariable String projectId) {
        return issuesService.getPluginCustomFields(projectId);
    }

    @GetMapping("/demand/list/{projectId}")
    public List getDemandList(@PathVariable String projectId) {
        return issuesService.getDemandList(projectId);
    }

    @GetMapping("/third/part/template/enable/{projectId}")
    public boolean thirdPartTemplateEnable(@PathVariable String projectId) {
        return issuesService.thirdPartTemplateEnable(projectId);
    }

    @PostMapping("/platform/transitions")
    public List<PlatformStatusDTO> getPlatformTransitions(@RequestBody PlatformIssueTypeRequest request) {
        return issuesService.getPlatformTransitions(request);
    }

    @PostMapping("/platform/status")
    public List<PlatformStatusDTO> getPlatformStatus(@RequestBody PlatformIssueTypeRequest request) {
        return issuesService.getPlatformStatus(request);
    }

    @GetMapping("/platform/option")
    public List<SelectOption> getPlatformOptions() {
        return platformPluginService.getPlatformOptions();
    }

    @PostMapping("/platform/form/option")
    public List<SelectOption> getPlatformOptions(@RequestBody PlatformOptionRequest request) {
        return platformPluginService.getFormOption(request);
    }

    @PostMapping("/check/third/project")
    public void checkThirdProjectExist(@RequestBody Project project) {
        issuesService.checkThirdProjectExist(project);
    }

    @GetMapping("/import/template/download/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_CREATE)
    public void downloadImportTemplate(@PathVariable String projectId, HttpServletResponse response) {
        issuesService.issueImportTemplate(projectId, response);
    }

    @PostMapping("/import")
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.IMPORT, project = "#request.projectId")
    public ExcelResponse issueImport(@RequestPart("request") IssueImportRequest request,  @RequestPart("file") MultipartFile file) {
        baseCheckPermissionService.checkProjectOwner(request.getProjectId());
        return issuesService.issueImport(request, file);
    }

    @PostMapping("/export")
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.EXPORT, project = "#exportRequest.projectId")
    public void exportIssues(@RequestBody IssueExportRequest exportRequest, HttpServletResponse response) {
        issuesService.issueExport(exportRequest, response);
    }
}
