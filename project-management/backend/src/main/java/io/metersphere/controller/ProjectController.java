package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.AddProjectRequest;
import io.metersphere.request.ProjectRequest;
import io.metersphere.request.member.AddMemberRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.ProjectService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @GetMapping("/list/all")
    public List<ProjectDTO> listAll() {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        return projectService.getProjectList(request);
    }

    @GetMapping("/list/all/{workspaceId}")
    @RequiresPermissions(value = {PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ, PermissionConstants.SYSTEM_USER_READ}, logical = Logical.OR)
    public List<ProjectDTO> listAll(@PathVariable String workspaceId) {
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(workspaceId);
        return projectService.getProjectList(request);
    }

    @GetMapping("/recent/{count}")
    public List<Project> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return projectService.getRecentProjectList(request);
    }

    @GetMapping("/member/size/{id}")
    public long getProjectMemberSize(@PathVariable String id) {
        return projectService.getProjectMemberSize(id);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ)
    public Pager<List<ProjectDTO>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, projectService.getProjectList(request));
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#project.id)", content = "#msClass.getLogDetails(#project.id)", msClass = BaseProjectService.class)
    @RequiresPermissions(value = {PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_EDIT, PermissionConstants.PROJECT_MANAGER_READ_EDIT}, logical = Logical.OR)
    public void updateProject(@RequestBody AddProjectRequest project) {
        projectService.updateProject(project);
    }

    @PostMapping("/member/update")
    @RequiresPermissions("PROJECT_USER:READ+EDIT")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MEMBER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#memberDTO)", content = "#msClass.getLogDetails(#memberDTO)", msClass = BaseProjectService.class)
    public void updateMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        projectService.updateMember(memberDTO);
    }

    @GetMapping("/get-owner-project-ids")
    public Collection<String> getOwnerProjectIds() {
        return baseCheckPermissionService.getUserRelatedProjectIds();
    }

    @GetMapping("/get-owner-projects")
    public List<ProjectDTO> getOwnerProjects() {
        return baseCheckPermissionService.getOwnerProjects();
    }

    @GetMapping("/gen-tcp-mock-port/{id}")
    public String genTcpMockPort(@PathVariable String id) {
        return projectService.genTcpMockPort(id);
    }

    @PostMapping("/check/third/project")
    public void checkThirdProjectExist(@RequestBody Project project) {
        projectService.checkThirdProjectExist(project);
    }

    @PostMapping("/member/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_USER_READ)
    public Pager<List<User>> getProjectMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, baseUserService.getProjectMemberList(request));
    }

    @GetMapping("/member/delete/{projectId}/{userId}")
    @RequiresPermissions(PermissionConstants.PROJECT_USER_READ_DELETE)
    public void deleteProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        baseUserService.deleteProjectMember(projectId, userId);
    }

    @GetMapping("/service/integration/all")
    public List<ServiceIntegration> getAll() {
        return projectService.getAllServiceIntegration();
    }

    @PostMapping("/member/add")
    @RequiresPermissions("PROJECT_USER:READ+CREATE")
    public void addProjectMember(@RequestBody AddMemberRequest request) {
        projectService.addProjectMember(request);
    }
}
