package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReviewFollower;
import io.metersphere.functional.domain.CaseReviewFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CaseReviewFollowerMapper {
    long countByExample(CaseReviewFollowerExample example);

    int deleteByExample(CaseReviewFollowerExample example);

    int deleteByPrimaryKey(@Param("reviewId") String reviewId, @Param("userId") String userId);

    int insert(CaseReviewFollower record);

    int insertSelective(CaseReviewFollower record);

    List<CaseReviewFollower> selectByExample(CaseReviewFollowerExample example);

    int updateByExampleSelective(@Param("record") CaseReviewFollower record, @Param("example") CaseReviewFollowerExample example);

    int updateByExample(@Param("record") CaseReviewFollower record, @Param("example") CaseReviewFollowerExample example);

    int batchInsert(@Param("list") List<CaseReviewFollower> list);

    int batchInsertSelective(@Param("list") List<CaseReviewFollower> list, @Param("selective") CaseReviewFollower.Column ... selective);
}