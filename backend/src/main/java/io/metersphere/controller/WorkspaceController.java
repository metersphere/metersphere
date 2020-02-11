package io.metersphere.controller;

import io.metersphere.base.domain.Workspace;
import io.metersphere.service.WorkspaceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;

    @PostMapping("add")
    public Workspace addWorkspace(@RequestBody Workspace workspace) {
        return workspaceService.add(workspace);
    }

    @PostMapping("list")
    public List<Workspace> getWorkspaceList() {
        return workspaceService.getWorkspaceList();
    }
}
