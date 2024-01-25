package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioCsvStep;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import io.metersphere.api.dto.scenario.CsvVariable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-16  19:57
 */
public interface ExtApiScenarioStepMapper {
    List<String> getStepIdsByScenarioId(@Param("scenarioId") String scenarioId);

    List<ApiScenarioStepDTO> getStepDTOByScenarioIds(@Param("scenarioIds") List<String> scenarioIds);

    List<CsvVariable> getCsvVariableByScenarioId(@Param("id") String id);

    List<ApiScenarioCsvStep> getCsvStepByStepIds(@Param("ids") List<String> stepIds);
}
