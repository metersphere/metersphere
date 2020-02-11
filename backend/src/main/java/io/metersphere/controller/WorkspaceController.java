package io.metersphere.controller;

import io.metersphere.base.domain.Workspace;
import io.metersphere.service.WorkspaceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;

    @PostMapping("save")
    public Workspace saveWorkspace(@RequestBody Workspace workspace) {
        return workspaceService.saveWorkspace(workspace);
    }

    @GetMapping("delete/{workspaceId}")
    public void saveWorkspace(@PathVariable String workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
    }

    @PostMapping("list")
    public List<Workspace> getWorkspaceList() {
        return workspaceService.getWorkspaceList();
    }
}
