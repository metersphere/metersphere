package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewFollow;
import io.metersphere.functional.domain.CaseReviewFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewFollowMapper {
    long countByExample(CaseReviewFollowExample example);

    int deleteByExample(CaseReviewFollowExample example);

    int deleteByPrimaryKey(@Param("reviewId") String reviewId, @Param("followId") String followId);

    int insert(CaseReviewFollow record);

    int insertSelective(CaseReviewFollow record);

    List<CaseReviewFollow> selectByExample(CaseReviewFollowExample example);

    int updateByExampleSelective(@Param("record") CaseReviewFollow record, @Param("example") CaseReviewFollowExample example);

    int updateByExample(@Param("record") CaseReviewFollow record, @Param("example") CaseReviewFollowExample example);
}