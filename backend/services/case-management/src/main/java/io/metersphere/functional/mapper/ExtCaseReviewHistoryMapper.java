package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtCaseReviewHistoryMapper {

    List<CaseReviewHistoryDTO> list(@Param("caseId") String caseId, @Param("reviewId") String reviewId);

    List<CaseReviewHistoryDTO> resultList(@Param("caseId") String caseId, @Param("reviewId") String reviewId);


    List<CaseReviewHistoryDTO> getHistoryListWidthAbandoned(@Param("caseId") String caseId, @Param("reviewId") String reviewId);

    List<CaseReviewHistoryDTO> getHistoryListWidthCaseId(@Param("caseId") String caseId, @Param("reviewId") String reviewId);

    void updateDelete(@Param("caseIds") List<String> caseIds, @Param("reviewId") String reviewId, @Param("delete") boolean delete);

    void updateAbandoned(@Param("caseId") String caseId);

    void batchUpdateAbandoned(@Param("reviewId") String reviewId, @Param("caseIds") List<String> caseIds);

    List<CaseReviewHistory> getReviewHistoryStatus(@Param("caseIds") List<String> caseIds, @Param("reviewId") String reviewId);
}
