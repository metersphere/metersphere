package io.metersphere.controller;

import io.metersphere.base.domain.Role;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.service.UserRoleService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
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
    @RequiresRoles(value = {RoleConstants.ADMIN,RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public List<Role> getOrganizationMemberRoles(@PathVariable String orgId, @PathVariable String userId) {
        return userRoleService.getOrganizationMemberRoles(orgId, userId);
    }

    @GetMapping("/list/ws/{workspaceId}/{userId}")
    @RequiresRoles(value = {RoleConstants.ADMIN,RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public List<Role> getWorkspaceMemberRole(@PathVariable String workspaceId, @PathVariable String userId) {
        return userRoleService.getWorkspaceMemberRoles(workspaceId, userId);
    }
}
