package io.metersphere.base.mapper.ext;

import io.metersphere.plan.dto.TestPlanApiScenarioInfoDTO;

import java.util.List;

public interface ExtTestPlanScenarioCaseMapper {
    List<TestPlanApiScenarioInfoDTO> selectLegalDataByTestPlanId(String planId);

    List<TestPlanApiScenarioInfoDTO> selectLegalUiDataByTestPlanId(String planId);
 }
