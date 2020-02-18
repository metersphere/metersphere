package io.metersphere.controller.request.member;

import java.util.List;

public class AddMemberRequest {

    private String workspaceId;

    private List<String> userIds;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
