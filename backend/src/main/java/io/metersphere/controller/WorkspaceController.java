package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.service.WorkspaceService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;

    @PostMapping("add")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Workspace addWorkspace(@RequestBody Workspace workspace) {
        return workspaceService.saveWorkspace(workspace);
    }

    @PostMapping("update")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Workspace updateWorkspace(@RequestBody Workspace workspace) {
        workspaceService.checkOwner(workspace.getId());
        return workspaceService.saveWorkspace(workspace);
    }

    @GetMapping("delete/{workspaceId}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public void deleteWorkspace(@PathVariable String workspaceId) {
        workspaceService.checkOwner(workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Pager<List<Workspace>> getWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getWorkspaceList());
    }
}
