package io.metersphere.controller;

import io.metersphere.base.domain.Workspace;
import io.metersphere.service.WorkspaceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;

    @PostMapping("add")
    public Workspace insertUser(@RequestBody Workspace workspace) {
        return workspaceService.add(workspace);
    }
}
