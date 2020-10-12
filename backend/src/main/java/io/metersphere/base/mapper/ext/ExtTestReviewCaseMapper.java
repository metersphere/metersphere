package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtTestReviewCaseMapper {

    List<TestReviewCaseDTO> list(@Param("request") QueryCaseReviewRequest request);
    List<String> getStatusByReviewId(String reviewId);
    List<String> findRelateTestReviewId(String userId, String workspaceId);
}
