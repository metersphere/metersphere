package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.service.ProjectService;
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
        // todo: 限制workspace和org
        return projectService.listAll();
    }

    @PostMapping("/add")
    public Project addProject(@RequestBody Project project) {
        return projectService.addProject(project);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<Project>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, projectService.getProjectList());
    }

    @GetMapping("/delete/{projectId}")
    public void deleteProject(@PathVariable(value = "projectId") String projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/update")
    public void updateProject(@RequestBody Project Project) {
        projectService.updateProject(Project);
    }
}
