package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.ReviewsDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtCaseReviewFunctionalCaseUserMapper {
    List<ReviewsDTO> selectReviewers(@Param("ids") List<String> ids, @Param("reviewId") String reviewId);

    void deleteByCaseIds(@Param("ids") List<String> ids, @Param("reviewId") String reviewId);
}