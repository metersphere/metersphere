package io.metersphere.controller.request;

import io.metersphere.commons.annotations.FuzzyQuery;

public class WorkspaceRequest {
    private String organizationId;
    @FuzzyQuery
    private String name;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
