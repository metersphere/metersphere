package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.service.ProjectService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("add")
    public Project add(@Validated({Created.class}) @RequestBody Project project) {
        return projectService.add(project);
    }

    @PostMapping("edit")
    public Project edit(@Validated({Updated.class}) @RequestBody Project project) {
        return projectService.edit(project);
    }
}
