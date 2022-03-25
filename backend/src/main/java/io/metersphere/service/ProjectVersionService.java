package io.metersphere.service;

import io.metersphere.base.domain.ProjectVersion;
import io.metersphere.controller.request.ProjectVersionRequest;
import io.metersphere.dto.ProjectVersionDTO;

import java.util.List;

public interface ProjectVersionService {
    List<ProjectVersionDTO> getVersionList(ProjectVersionRequest request);

    ProjectVersion addProjectVersion(ProjectVersion projectVersion);

    ProjectVersion getProjectVersion(String id);

    ProjectVersion editProjectVersion(ProjectVersion projectVersion);

    void deleteProjectVersion(String id);

    List<ProjectVersionDTO> getProjectVersions(String projectId);

    void changeStatus(String id, String status);

    List<ProjectVersion> getProjectVersionByIds(List<String> versionIds);
}
