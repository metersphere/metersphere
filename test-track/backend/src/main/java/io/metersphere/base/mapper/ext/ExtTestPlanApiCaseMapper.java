package io.metersphere.base.mapper.ext;

import io.metersphere.plan.dto.TestPlanApiCaseInfoDTO;

import java.util.List;

public interface ExtTestPlanApiCaseMapper {
    List<TestPlanApiCaseInfoDTO> selectLegalDataByTestPlanId(String planId);
}

