package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseReviewTestCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewTestCaseMapper {
    List<TestCase> getTestCaseWithNodeInfo(@Param("reviewId") String reviewId);

    List<TestCaseReviewTestCase> getCaseStatusByReviewIds(@Param("reviewIds") List<String> reviewIds);

    List<TestCaseReviewTestCase> selectForRuleChange(@Param("reviewId") String reviewId);

    List<TestCaseReviewTestCase> selectForReReview(@Param("caseId") String caseId);
}
