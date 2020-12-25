package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioModuleMapper {
    List<ApiScenarioModuleDTO> getNodeTreeByProjectId(@Param("projectId") String projectId);

    void updatePos(String id, Double pos);
}
