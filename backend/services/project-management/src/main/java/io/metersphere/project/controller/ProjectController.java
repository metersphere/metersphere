package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @GetMapping("list-all")
    public List<Project> selectAll() {
        return projectService.list();
    }
}
