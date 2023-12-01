package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.CaseReviewPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtCaseReviewMapper {

    List<CaseReview> checkCaseByModuleIds(@Param("moduleIds") List<String> deleteIds);

    Long getPos(@Param("projectId") String projectId);

    List<CaseReviewDTO> list(@Param("request") CaseReviewPageRequest request);

}
