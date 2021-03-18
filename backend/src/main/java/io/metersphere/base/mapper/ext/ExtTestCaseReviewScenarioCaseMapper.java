package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.TestPlanScenarioRequest;
import io.metersphere.base.domain.TestCaseReviewScenario;
import io.metersphere.base.domain.TestPlanApiScenario;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewScenarioCaseMapper {
    void insertIfNotExists(@Param("request") TestCaseReviewScenario request);

    List<ApiScenarioDTO> list(@Param("request") TestPlanScenarioRequest request);

    List<String> getExecResultByReviewId(String reviewId);

    List<String> getIdsByReviewId(String reviewId);

    List<String> getNotRelevanceCaseIds(String planId, List<String> relevanceProjectIds);
}
