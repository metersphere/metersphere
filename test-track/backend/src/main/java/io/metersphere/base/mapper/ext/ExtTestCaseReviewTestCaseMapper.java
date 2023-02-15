package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCaseReviewTestCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewTestCaseMapper {

    List<TestCaseReviewTestCase> getCaseStatusByReviewIds(@Param("reviewIds") List<String> reviewIds);

    List<TestCaseReviewTestCase> selectForReviewChange(@Param("reviewId") String reviewId);

    List<TestCaseReviewTestCase> selectForReReview(@Param("caseId") String caseId);

    List<TestCaseReviewTestCase> selectForReviewerChange(@Param("reviewId") String reviewId);
}
