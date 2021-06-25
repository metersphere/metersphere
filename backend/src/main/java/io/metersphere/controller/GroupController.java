package io.metersphere.controller;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.Organization;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.GroupRequest;
import io.metersphere.controller.request.group.EditGroupRequest;
import io.metersphere.dto.GroupDTO;
import io.metersphere.dto.GroupPermissionDTO;
import io.metersphere.service.GroupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@RequestMapping("/user/group")
@RestController
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping("/get/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ)
    public Pager<List<GroupDTO>> getGroupList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EditGroupRequest request) {
        request.setGoPage(goPage);
        request.setPageSize(pageSize);
        return groupService.getGroupList(request);
    }

    @GetMapping("/get/all")
    public List<Group> getAllGroup() {
        return groupService.getAllGroup();
    }

    @PostMapping("/get")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ)
    public List<Group> getGroupByType(@RequestBody EditGroupRequest request) {
        return groupService.getGroupByType(request);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ_CREATE)
    public Group addGroup(@RequestBody EditGroupRequest request) {
        return groupService.addGroup(request);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ_EDIT)
    public void editGroup(@RequestBody EditGroupRequest request) {
        groupService.editGroup(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ_DELETE)
    public void deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
    }

    @PostMapping("/permission")
    public GroupPermissionDTO getGroupResource(@RequestBody Group group) {
        return groupService.getGroupResource(group);
    }

    @PostMapping("/permission/edit")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ_SETTING_PERMISSION)
    public void editGroupPermission(@RequestBody EditGroupRequest editGroupRequest) {
        groupService.editGroupPermission(editGroupRequest);
    }

    @GetMapping("/all/{userId}")
    public List<Map<String, Object>> getAllUserGroup(@PathVariable("userId") String userId) {
        return groupService.getAllUserGroup(userId);
    }

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_GROUP_READ)
    public List<Group> getGroupsByType(@RequestBody GroupRequest request) {
        return groupService.getGroupsByType(request);
    }

    @GetMapping("/list/org/{orgId}/{userId}")
    public List<Group> getOrganizationMemberGroups(@PathVariable String orgId, @PathVariable String userId) {
        return groupService.getOrganizationMemberGroups(orgId, userId);
    }

    @GetMapping("/list/ws/{workspaceId}/{userId}")
    public List<Group> getWorkspaceMemberGroups(@PathVariable String workspaceId, @PathVariable String userId) {
        return groupService.getWorkspaceMemberGroups(workspaceId, userId);
    }

    @GetMapping("/list/project/{projectId}/{userId}")
    public List<Group> getProjectMemberGroups(@PathVariable String projectId, @PathVariable String userId) {
        return groupService.getProjectMemberGroups(projectId, userId);
    }

    @GetMapping("/org/{userId}")
    public List<Organization> getOrganization(@PathVariable String userId) {
        return groupService.getOrganization(userId);
    }

    @GetMapping("/{type}/{id}")
    public List<?> getResource(@PathVariable String type, @PathVariable String id) {
        return groupService.getResource(type, id);
    }
}
