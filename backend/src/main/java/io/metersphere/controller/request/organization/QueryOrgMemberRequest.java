package io.metersphere.controller.request.organization;

import io.metersphere.commons.constants.RoleConstants;

public class QueryOrgMemberRequest {

    private String name;
    private String organizationId;
    private String roleId = RoleConstants.ORG_ADMIN;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
