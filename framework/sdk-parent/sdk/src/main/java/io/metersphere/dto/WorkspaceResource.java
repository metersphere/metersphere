package io.metersphere.dto;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.Workspace;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkspaceResource {
    private List<Workspace> workspaces = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
}
