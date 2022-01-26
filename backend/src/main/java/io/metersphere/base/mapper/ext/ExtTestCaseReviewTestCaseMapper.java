package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewTestCaseMapper {
    List<TestCase> getTestCaseWithNodeInfo(@Param("reviewId") String reviewId);
}
