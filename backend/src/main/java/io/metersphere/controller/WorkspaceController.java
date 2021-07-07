package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.WorkspaceRequest;
import io.metersphere.dto.WorkspaceDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.OrganizationService;
import io.metersphere.service.UserService;
import io.metersphere.service.WorkspaceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserService userService;

    @PostMapping("add")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    public Workspace addWorkspace(@RequestBody Workspace workspace) {
        organizationService.checkOrgOwner(workspace.getOrganizationId());
        return workspaceService.saveWorkspace(workspace);
    }

    @GetMapping("/list")
    public List<Workspace> getWorkspaceList() {
        return workspaceService.getWorkspaceList(new WorkspaceRequest());
    }

    @PostMapping("special/add")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    public Workspace addWorkspaceByAdmin(@RequestBody Workspace workspace) {
        return workspaceService.addWorkspaceByAdmin(workspace);
    }

    @PostMapping("update")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#workspace.id)", content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    public Workspace updateWorkspace(@RequestBody Workspace workspace) {
//        workspaceService.checkWorkspaceOwnerByOrgAdmin(workspace.getId());
        return workspaceService.saveWorkspace(workspace);
    }

    @PostMapping("special/update")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#workspace.id)", content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    public void updateWorkspaceByAdmin(@RequestBody Workspace workspace) {
        workspaceService.updateWorkspaceByAdmin(workspace);
    }

    @GetMapping("special/delete/{workspaceId}")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#workspaceId)", msClass = WorkspaceService.class)
    public void deleteWorkspaceByAdmin(@PathVariable String workspaceId) {
        userService.refreshSessionUser("workspace", workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @GetMapping("delete/{workspaceId}")
    @MsAuditLog(module = "system_workspace", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#workspaceId)", msClass = WorkspaceService.class)
    public void deleteWorkspace(@PathVariable String workspaceId) {
//        workspaceService.checkWorkspaceOwnerByOrgAdmin(workspaceId);
        userService.refreshSessionUser("workspace", workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<Workspace>> getWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody WorkspaceRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getWorkspaceList(request));
    }

    @PostMapping("list/all/{goPage}/{pageSize}")
    public Pager<List<WorkspaceDTO>> getAllWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody WorkspaceRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getAllWorkspaceList(request));
    }

    @GetMapping("/list/userworkspace/{userId}")
    public List<Workspace> getWorkspaceListByUserId(@PathVariable String userId) {
        return workspaceService.getWorkspaceListByUserId(userId);
    }

    @GetMapping("/list/orgworkspace/{userId}/{orgId}")
    public List<Workspace> getWorkspaceListByOrgId(@PathVariable String userId, @PathVariable String orgId) {
        return workspaceService.getWorkspaceListByOrgIdAndUserId(userId, orgId);
    }

    @PostMapping("/member/update")
    @MsAuditLog(module = "workspace_member", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#memberDTO)", content = "#msClass.getLogDetails(#memberDTO)", msClass = WorkspaceService.class)
    public void updateOrgMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        workspaceService.updateWorkspaceMember(memberDTO);
    }

    @GetMapping("/list/{orgId}")
    public List<Workspace> getWorkspaceByOrgId(@PathVariable String orgId) {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setOrganizationId(orgId);
        return workspaceService.getWorkspaceList(request);
    }
}
