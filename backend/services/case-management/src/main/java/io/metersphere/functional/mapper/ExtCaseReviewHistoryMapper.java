package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtCaseReviewHistoryMapper {

    List<CaseReviewHistoryDTO> list(@Param("caseId") String caseId, @Param("reviewId") String reviewId);

}
