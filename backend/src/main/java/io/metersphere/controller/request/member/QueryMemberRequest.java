package io.metersphere.controller.request.member;

import io.metersphere.commons.constants.RoleConstants;

public class QueryMemberRequest {
    private String name;
    private String workspaceId;
    private String roleId = RoleConstants.TEST_MANAGER;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
