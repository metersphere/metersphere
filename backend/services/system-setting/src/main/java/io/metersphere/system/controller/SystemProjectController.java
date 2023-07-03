package io.metersphere.system.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.request.ProjectMemberRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
    @Log(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_PROJECT, details = "#project.name")
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
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }

    @PostMapping("/update")
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            sourceId = "#project.id", details = "#project.name")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_UPDATE)
    public void updateProject(@RequestBody @Validated({Updated.class}) Project project) {
        project.setUpdateUser(SessionUtils.getUserId());
        systemProjectService.update(project);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    @Log(isBefore = true, type = OperationLogType.DELETE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#id)", msClass = SystemProjectService.class, sourceId = "#id")
    public void deleteProject(@PathVariable String id) {
        Project project = new Project();
        project.setId(id);
        project.setDeleteUser(SessionUtils.getUserId());
        systemProjectService.delete(project);
    }

    @GetMapping("/revoke/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_DELETE)
    @Log(isBefore = true, type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PROJECT,
            details = "#msClass.getLogDetails(#id)", msClass = SystemProjectService.class, sourceId = "#id")
    public void revokeProject(@PathVariable String id) {
        systemProjectService.revoke(id);
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    public Pager<List<User>> getProjectMember(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectMember(request));
    }

    @PostMapping("/add-member")
    @RequiresPermissions(PermissionConstants.SYSTEM_PROJECT_READ_ADD_USER)
    public void addProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        request.setCreateUser(SessionUtils.getUserId());
        systemProjectService.addProjectMember(request);
    }

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
