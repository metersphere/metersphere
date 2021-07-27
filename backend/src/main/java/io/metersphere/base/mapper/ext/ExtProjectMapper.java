package io.metersphere.base.mapper.ext;

import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProjectMapper {

    List<ProjectDTO> getProjectWithWorkspace(@Param("proRequest") ProjectRequest request);

    List<String> getProjectIdByWorkspaceId(String workspaceId);

    int removeIssuePlatform(@Param("platform") String platform, @Param("orgId") String orgId);

    List<ProjectDTO> getUserProject(@Param("proRequest") ProjectRequest request);

    String getSystemIdByProjectId(String projectId);

    List<String> getProjectIds();

    String getMaxSystemId();
}
