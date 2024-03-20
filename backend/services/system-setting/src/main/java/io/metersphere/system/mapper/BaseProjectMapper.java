package io.metersphere.system.mapper;

import io.metersphere.project.domain.Project;
import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseProjectMapper {
    Project selectOne();

    List<Project> selectProjectByIdList(List<String> projectIds);

    List<String> getProjectIdByOrgId(@Param("orgId") String orgId);

    List<OptionDTO> getProjectOptionsById(@Param("id") String id);

    /**
     * 获取所有项目
     *
     * @return 所有项目
     */
    List<OptionDTO> getProjectOptions();

    /**
     * 获取组织下的所有项目
     *
     * @param orgId 组织ID
     * @return 组织下的所有项目
     */
    List<OptionDTO> getProjectOptionsByOrgId(@Param("orgId") String orgId);

    List<Project> selectProjectByLimit(@Param("start") int start, @Param("limit") int limit);
}
