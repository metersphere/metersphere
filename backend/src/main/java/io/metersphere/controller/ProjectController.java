package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.service.ProjectService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @GetMapping("/listAll")
    public List<Project> listAll() {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        return projectService.getProjectList(request);
    }

    @GetMapping("/recent/{count}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<Project> recentProjects(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        ProjectRequest request = new ProjectRequest();
        request.setWorkspaceId(currentWorkspaceId);
        // 最近 `count` 个项目
        PageHelper.startPage(1, count);
        return projectService.getRecentProjectList(request);
    }

    @PostMapping("/add")
    @RequiresRoles(RoleConstants.TEST_MANAGER)
    public Project addProject(@RequestBody Project project) {
        return projectService.addProject(project);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.TEST_MANAGER)
    public Pager<List<Project>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ProjectRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, projectService.getProjectList(request));
    }

    @GetMapping("/delete/{projectId}")
    @RequiresRoles(RoleConstants.TEST_MANAGER)
    public void deleteProject(@PathVariable(value = "projectId") String projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/update")
    @RequiresRoles(RoleConstants.TEST_MANAGER)
    public void updateProject(@RequestBody Project Project) {
        projectService.updateProject(Project);
    }
}
