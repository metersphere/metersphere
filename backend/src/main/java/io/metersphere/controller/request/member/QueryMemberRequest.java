package io.metersphere.controller.request.member;

import io.metersphere.commons.constants.RoleConstants;

public class QueryMemberRequest {
    private String workspaceId;
    private String roleId = RoleConstants.TESTMANAGER.getValue();

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
