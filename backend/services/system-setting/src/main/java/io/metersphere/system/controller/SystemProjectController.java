package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.log.annotation.RequestLog;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/project")
public class SystemProjectController {
    @Resource
    private SystemProjectService systemProjectService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD)
    @RequestLog(isBefore = true, type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    public Project addProject(@RequestBody @Validated({Created.class}) Project project) {
        project.setCreateUser(SessionUtils.getUserId());
        return systemProjectService.add(project);
    }


    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ)
    public Project getProject(@PathVariable String id) {
        return systemProjectService.get(id);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ)
    public Pager<List<ProjectDTO>> getProjectList(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }

    @PostMapping("/update")
    @RequestLog(isBefore = true, type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_UPDATE)
    public void updateProject(@RequestBody @Validated({Updated.class}) Project project) {
        project.setUpdateUser(SessionUtils.getUserId());
        systemProjectService.update(project);
    }

    @PostMapping("/delete")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    public void deleteProject(@RequestBody @Validated({Updated.class}) Project project) {
        project.setDeleteUser(SessionUtils.getUserId());
        systemProjectService.delete(project);
    }

    @PostMapping("/revoke")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    public void revokeProject(@RequestBody @Validated({Updated.class}) Project project) {
        systemProjectService.revoke(project);
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    public Pager<List<User>> getProjectMember(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, systemProjectService.getProjectMember(request));
    }

   /* @PostMapping("/addMember/{projectId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    public void addProjectMember(@PathVariable String projectId, @RequestBody List<UserDTO> userDTOList) {
        systemProjectService.addProjectMember(projectId, userDTOList);
    }*/

    @GetMapping("/remove-member/{projectId}/{userId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE_USER)
    public void removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        systemProjectService.removeProjectMember(projectId, userId);
    }

    @GetMapping("/list/{organizationId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ)
    public List<Project> getProjectListByOrg(@PathVariable String organizationId) {
        return systemProjectService.getProjectListByOrg(organizationId);
    }


}
