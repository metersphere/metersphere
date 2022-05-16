package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Project;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtProjectMapper {

    List<ProjectDTO> getProjectWithWorkspace(@Param("proRequest") ProjectRequest request);

    List<String> getProjectIdByWorkspaceId(String workspaceId);

    int removeIssuePlatform(@Param("platform") String platform, @Param("workspaceId") String workspaceId);

    List<ProjectDTO> getUserProject(@Param("proRequest") ProjectRequest request);

    String getSystemIdByProjectId(String projectId);

    List<String> getProjectIds();

    List<Project> getProjectForCustomField(String workspaceId);

    String getMaxSystemId();

    @MapKey("id")
    Map<String, Project> queryNameByIds(@Param("ids") List<String> ids);

    Project selectProjectByResourceId(@Param("resourceId") String resourceId);

    long getProjectMemberSize(@Param("projectId") String projectId);

    List<Project> getProjectByUserId(@Param("userId") String userId);

    int getProjectPlanBugSize(@Param("projectId") String projectId);

    void setDefaultMessageTask(@Param("projectId") String projectId);

    List<ProjectDTO> queryListByIds(@Param("ids") List<String> ids);

    void updateUseDefaultCaseTemplateProject(@Param("originId") String originId,@Param("templateId") String templateId,@Param("projectId") String projectId);
}
