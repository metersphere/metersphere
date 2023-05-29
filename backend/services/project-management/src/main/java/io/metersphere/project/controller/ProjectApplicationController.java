package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("project/application")
public class ProjectApplicationController {
    @Resource
    private ProjectApplicationService projectApplicationService;

    @PostMapping("add")
    public ProjectApplication add(@Validated({Created.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.add(application);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public ProjectApplication update(@Validated({Updated.class}) @RequestBody ProjectApplication application) {
        return projectApplicationService.update(application);
    }

    @GetMapping("list/{projectId}")
    public List<ProjectApplication> getList(@PathVariable String projectId) {
        return projectApplicationService.list(projectId);
    }
}
