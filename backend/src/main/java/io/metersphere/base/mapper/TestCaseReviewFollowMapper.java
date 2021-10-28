package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewFollow;
import io.metersphere.base.domain.TestCaseReviewFollowExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewFollowMapper {
    long countByExample(TestCaseReviewFollowExample example);

    int deleteByExample(TestCaseReviewFollowExample example);

    int insert(TestCaseReviewFollow record);

    int insertSelective(TestCaseReviewFollow record);

    List<TestCaseReviewFollow> selectByExample(TestCaseReviewFollowExample example);

    int updateByExampleSelective(@Param("record") TestCaseReviewFollow record, @Param("example") TestCaseReviewFollowExample example);

    int updateByExample(@Param("record") TestCaseReviewFollow record, @Param("example") TestCaseReviewFollowExample example);
}