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

    /**
     * 查询有步骤详情的请求类型的步骤
     * 包括 接口定义，接口用例，自定义请求
     * 类型是  COPY 或者 DIRECT
     * @param scenarioId
     * @return
     */
    List<String> getHasBlobRequestStepIds(@Param("scenarioId")  String scenarioId);
}
