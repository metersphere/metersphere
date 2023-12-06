package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtCaseReviewFunctionalCaseMapper {

    List<FunctionalCaseReviewDTO> list(@Param("request") FunctionalCaseReviewListRequest request);

    void updateStatus(@Param("caseId") String caseId, @Param("reviewId") String reviewId, @Param("status") String status);

    Long getUnCompletedCaseCount(@Param("reviewId") String reviewId, @Param("statusList") List<String> statusList);
}
