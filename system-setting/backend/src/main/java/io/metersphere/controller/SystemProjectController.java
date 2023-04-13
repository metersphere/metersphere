package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.AddProjectRequest;
import io.metersphere.request.ProjectRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.MicroService;
import io.metersphere.service.SystemProjectService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "/project")
public class SystemProjectController {
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private MicroService microService;

    @GetMapping("/list/all")
    public List<ProjectDTO> listAll() {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        return systemProjectService.getProjectList(request);
    }

    @GetMapping("/list/all/{workspaceId}")
    @RequiresPermissions(value = {PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ, PermissionConstants.SYSTEM_USER_READ}, logical = Logical.OR)
    public List<ProjectDTO> listAll(@PathVariable String workspaceId) {
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(workspaceId);
        return systemProjectService.getProjectList(request);
    }

    @GetMapping("/recent/{count}")
    public List<Project> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return systemProjectService.getRecentProjectList(request);
    }

    @GetMapping("/member/size/{id}")
    public long getProjectMemberSize(@PathVariable String id) {
        return systemProjectService.getProjectMemberSize(id);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#project.id)", msClass = BaseProjectService.class)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_CREATE)
    public Project addProject(@RequestBody AddProjectRequest project, HttpServletRequest request) {
        Project p = systemProjectService.addProject(project);
        try {
            microService.getForData(MicroServiceName.API_TEST, "/api/definition/mock-environment/" + p.getId());
        } catch (Exception e) {
            LogUtil.error("调用 接口测试服务 /api/definition/mock-environment/ 失败");
        }
        return p;
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ)
    public Pager<List<ProjectDTO>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }

    @GetMapping("/delete/{projectId}")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#projectId)", msClass = BaseProjectService.class)
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_DELETE)
    public void deleteProject(@PathVariable(value = "projectId") String projectId) {
        systemProjectService.deleteProject(projectId);
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#project.id)", content = "#msClass.getLogDetails(#project.id)", msClass = BaseProjectService.class)
    @RequiresPermissions(value = {PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ_EDIT, PermissionConstants.PROJECT_MANAGER_READ_EDIT}, logical = Logical.OR)
    public void updateProject(@RequestBody AddProjectRequest project) {
        systemProjectService.updateProject(project);
    }

    @PostMapping("/member/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MEMBER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#memberDTO)", content = "#msClass.getLogDetails(#memberDTO)", msClass = BaseProjectService.class)
    public void updateMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        systemProjectService.updateMember(memberDTO);
    }

    @GetMapping("/get-owner-project-ids")
    public Collection<String> getOwnerProjectIds() {
        return baseCheckPermissionService.getUserRelatedProjectIds();
    }

    @GetMapping("/get-owner-projects")
    public List<ProjectDTO> getOwnerProjects() {
        return baseCheckPermissionService.getOwnerProjects();
    }

    @GetMapping({"/field/template/{type}/option/{projectId}", "/field/template/{type}/option"})
    public Object getFieldTemplateOption(@PathVariable String type, @PathVariable(required = false) String projectId) {
        return microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, String.format("/field/template/%s/option/%s", type, projectId));
    }

    @PostMapping("/check/third/project")
    public void checkThirdProjectExist(@RequestBody Project project) {
        microService.postForData(MicroServiceName.TEST_TRACK, "/issues/check/third/project", project);
    }
}
