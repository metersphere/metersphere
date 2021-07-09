package io.metersphere.dto;

import io.metersphere.base.domain.Organization;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.Workspace;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizationResource {
    private List<Organization> organizations = new ArrayList<>();
    private List<Workspace> workspaces = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
}
