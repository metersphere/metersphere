package io.metersphere.system.mapper;

import io.metersphere.project.domain.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseProjectMapper {
    Project selectOne();

    List<Project> selectProjectByIdList(List<String> projectIds);

    List<String> getProjectIdByOrgId(@Param("orgId") String orgId);
}
