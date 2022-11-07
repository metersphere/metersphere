package io.metersphere.controller.ext;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiProjectRequest;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.request.ProjectRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.ext.ExtProjectApplicationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping()
public class ExtProjectController {
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private ExtProjectApplicationService extProjectApplicationService;

    @GetMapping("/api/project/member/size/{id}")
    public long getProjectMemberSize(@PathVariable String id) {
        return baseProjectService.getProjectMemberSize(id);
    }

    @PostMapping("/api/project/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.WORKSPACE_PROJECT_MANAGER_READ)
    public Pager<List<ProjectDTO>> getProjectList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, baseProjectService.getProjectList(request));
    }

    @GetMapping("/api/project/get/owner/ids")
    public Collection<String> getOwnerProjectIds() {
        return baseCheckPermissionService.getUserRelatedProjectIds();
    }

    @GetMapping("/api/project/get/owner/details")
    public List<ProjectDTO> getOwnerProjects() {
        return baseCheckPermissionService.getOwnerProjects();
    }

    @GetMapping("/api/current/user/{resourceId}")
    public void updateCurrentUserByResourceId(@PathVariable String resourceId) {
        extProjectApplicationService.updateCurrentUserByResourceId(resourceId);
    }

    @PostMapping("/list/related")
    public List<ProjectDTO> getUserProject(@RequestBody ApiProjectRequest request) {
        return extProjectApplicationService.getUserProject(request);
    }
}
