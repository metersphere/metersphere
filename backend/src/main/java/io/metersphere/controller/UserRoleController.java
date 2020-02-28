package io.metersphere.controller;

import io.metersphere.base.domain.Role;
import io.metersphere.service.UserRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RequestMapping("userrole")
@RestController
public class UserRoleController {

    @Resource
    private UserRoleService userRoleService;

    @GetMapping("/list/org/{orgId}/{userId}")
    public List<Role> getOrganizationMemberRoles(@PathVariable String orgId, @PathVariable String userId) {
        return userRoleService.getOrganizationMemberRoles(orgId, userId);
    }

    @GetMapping("/list/ws/{workspaceId}/{userId}")
    public List<Role> workspaceId(@PathVariable String workspaceId, @PathVariable String userId) {
        return userRoleService.getWorkspaceMemberRoles(workspaceId, userId);
    }
}
