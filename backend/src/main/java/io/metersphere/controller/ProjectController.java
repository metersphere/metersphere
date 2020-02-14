package io.metersphere.controller;

import io.metersphere.base.domain.Project;
import io.metersphere.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    @GetMapping("/listAll")
    public List<Project> listAll() {
        /// todo: 限制workspace和org
        return projectService.listAll();
    }
}
