package io.metersphere.base.mapper.ext;

import io.metersphere.environment.dto.ApiModuleDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface BaseApiTestEnvironmentMapper {
    List<String> selectNameByIds(@Param("ids") Collection<String> envIdSet);

    String selectNameById(String id);

    List<ApiModuleDTO> getNodeTreeByProjectId(@Param("projectId") String projectId, @Param("protocol") String protocol);

    List<String> selectNameByIdList(@Param("ids") List<String> envIdList);
}
