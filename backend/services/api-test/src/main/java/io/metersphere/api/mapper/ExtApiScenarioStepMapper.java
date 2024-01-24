package io.metersphere.api.mapper;

import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-16  19:57
 */
public interface ExtApiScenarioStepMapper {
    List<String> getStepIdsByScenarioId(@Param("scenarioId") String scenarioId);

    List<ApiScenarioStepDTO> getStepDTOByScenarioIds(@Param("scenarioIds") List<String> scenarioIds);
}
