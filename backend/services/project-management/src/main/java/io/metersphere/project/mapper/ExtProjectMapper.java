package io.metersphere.project.mapper;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.system.domain.TestResourcePool;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtProjectMapper {

    List<Project> getUserProject(@Param("organizationId") String organizationId, @Param("userId") String userId);

    String getModuleSetting(@Param("projectId") String projectId);

    List<Project> getProject(@Param("userId") String userId);

    List<Project> getAllProjectByOrgId(@Param("organizationId") String organizationId);

    List<Project> getProjectByOrgId(@Param("userId") String userId, @Param("organizationId") String organizationId);

    int resourcePoolIsExist(@Param("poolId") String poolId, @Param("projectId") String projectId);

    int resourcePoolIsExistByOrg(@Param("poolId") String string, @Param("projectId") String projectId);

    List<ProjectTestResourcePool> getResourcePool(@Param("projectId") String projectId);

    List<Project> getAllProject(@Param("organizationId") String organizationId);
    List<TestResourcePool> getResourcePoolOption(@Param("projectId") String projectId, @Param("type") String type);

    List<Project> getAllProjectWidthModule(@Param("organizationId") String organizationId, @Param("module") String module);

    List<Project> getUserProjectWidthModule(@Param("organizationId") String organizationId, @Param("userId") String userId, @Param("module") String module);

    List<Project> getProjectNameModule(@Param("organizationId") String organizationId, @Param("ids") List<String>projectIds);



}
