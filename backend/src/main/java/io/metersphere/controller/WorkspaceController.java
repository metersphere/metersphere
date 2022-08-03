package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.WorkspaceRequest;
import io.metersphere.dto.WorkspaceDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.dto.WorkspaceResource;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.security.session.RefreshSession;
import io.metersphere.service.UserService;
import io.metersphere.service.WorkspaceService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private UserService userService;

    @GetMapping("/list")
    @RequiresPermissions(value = {
            PermissionConstants.SYSTEM_GROUP_READ_CREATE,
            PermissionConstants.SYSTEM_GROUP_READ_EDIT,
            PermissionConstants.SYSTEM_USER_READ_CREATE,
            PermissionConstants.SYSTEM_USER_READ_EDIT,
            PermissionConstants.SYSTEM_WORKSPACE_READ
    }, logical = Logical.OR)
    public List<Workspace> getWorkspaceList() {
        return workspaceService.getWorkspaceList(new WorkspaceRequest());
    }

    @PostMapping("special/add")
    @MsAuditLog(module = OperLogModule.SYSTEM_WORKSPACE, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ_CREATE)
    @RefreshSession
    public Workspace addWorkspaceByAdmin(@RequestBody Workspace workspace) {
        return workspaceService.addWorkspaceByAdmin(workspace);
    }

    @PostMapping("special/update")
    @MsAuditLog(module = OperLogModule.SYSTEM_WORKSPACE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#workspace.id)", content = "#msClass.getLogDetails(#workspace.id)", msClass = WorkspaceService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ_EDIT)
    public void updateWorkspaceByAdmin(@RequestBody Workspace workspace) {
        workspaceService.updateWorkspaceByAdmin(workspace);
    }

    @GetMapping("special/delete/{workspaceId}")
    @MsAuditLog(module = OperLogModule.SYSTEM_WORKSPACE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#workspaceId)", msClass = WorkspaceService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ_DELETE)
    public void deleteWorkspaceByAdmin(@PathVariable String workspaceId) {
        userService.refreshSessionUser("workspace", workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @PostMapping("list/all/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_WORKSPACE_READ)
    public Pager<List<WorkspaceDTO>> getAllWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody WorkspaceRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getAllWorkspaceList(request));
    }

    @GetMapping("/list/userworkspace")
    public List<Workspace> getWorkspaceListByUserId() {
        return workspaceService.getWorkspaceListByUserId(SessionUtils.getUserId());
    }

    @PostMapping("/member/update")
    @MsAuditLog(module = OperLogModule.WORKSPACE_MEMBER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#memberDTO)", content = "#msClass.getLogDetails(#memberDTO)", msClass = WorkspaceService.class)
    public void updateOrgMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        workspaceService.updateWorkspaceMember(memberDTO);
    }

    @GetMapping("/list/resource/{groupId}/{type}")
    public WorkspaceResource listResource(@PathVariable String groupId, @PathVariable String type) {
        return workspaceService.listResource(groupId, type);
    }
}
