package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCaseReviewScenario;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewScenarioCaseMapper {
    void insertIfNotExists(@Param("request") TestCaseReviewScenario request);

    List<String> getExecResultByReviewId(String reviewId);

    List<String> getIdsByReviewId(String reviewId);

    List<String> getNotRelevanceCaseIds(String planId, List<String> relevanceProjectIds);
}
