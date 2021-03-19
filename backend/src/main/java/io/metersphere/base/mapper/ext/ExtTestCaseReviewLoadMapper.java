package io.metersphere.base.mapper.ext;

import io.metersphere.dto.TestReviewLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testreview.TestReviewRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewLoadMapper {
    List<String> selectIdsNotInPlan(@Param("projectId") String projectId, @Param("reviewId") String reviewId);
    List<TestReviewLoadCaseDTO> selectTestReviewLoadCaseList(@Param("request") TestReviewRequest request);
    void updateCaseStatus(@Param("reportId") String reportId, @Param("status") String status);
    List<String> getStatusByreviewId(@Param("reviewId") String reviewId);
}
