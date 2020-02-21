package io.metersphere.controller.request.organization;

import java.util.List;

public class AddOrgMemberRequest {

    private String organizationId;
    private List<String> userIds;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
