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

}
