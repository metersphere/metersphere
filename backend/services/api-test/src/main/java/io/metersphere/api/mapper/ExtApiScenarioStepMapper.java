package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioCsvStep;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-16  19:57
 */
public interface ExtApiScenarioStepMapper {
    List<String> getStepIdsByScenarioId(@Param("scenarioId") String scenarioId);

    /**
     * 这个 sql 执行时，会清理一级缓存
     * 避免多次调用后数据异常
     * @param scenarioIds
     * @return
     */
    List<ApiScenarioStepDTO> getStepDTOByScenarioIds(@Param("scenarioIds") List<String> scenarioIds);

    List<ApiScenarioCsvStep> getCsvStepByScenarioIds(@Param("scenarioIds") List<String> scenarioId);

    /**
     * 查询有步骤详情的请求类型的步骤
     * 包括 接口定义，接口用例，自定义请求
     * 类型是  COPY 或者 DIRECT
     * @param scenarioId
     * @return
     */
    List<String> getHasBlobRequestStepIds(@Param("scenarioId")  String scenarioId);

    List<String> selectResourceId(@Param("projectId") String projectId, @Param("stepType") String stepType);

    List<String> selectApiResourceId(@Param("projectId") String projectId, @Param("stepType") String stepType, List<String> protocols);

    List<String> selectApiCaseResourceId(@Param("projectId") String projectId, @Param("stepType") String stepType, List<String> protocols);

    List<String> selectCustomRequestConfigByProjectId(String projectId);
}
