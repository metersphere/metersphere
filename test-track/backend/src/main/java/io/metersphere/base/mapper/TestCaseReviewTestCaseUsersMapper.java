package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewTestCaseUsers;
import io.metersphere.base.domain.TestCaseReviewTestCaseUsersExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestCaseReviewTestCaseUsersMapper {

    long countByExample(TestCaseReviewTestCaseUsersExample example);

    int deleteByExample(TestCaseReviewTestCaseUsersExample example);

    int insert(TestCaseReviewTestCaseUsers example);

    int insertSelective(TestCaseReviewTestCaseUsers example);

    List<TestCaseReviewTestCaseUsers> selectByExample(TestCaseReviewTestCaseUsersExample example);

    int updateByExampleSelective(@Param("data") TestCaseReviewTestCaseUsers data, @Param("example") TestCaseReviewTestCaseUsersExample example);

    int updateByExample(@Param("data") TestCaseReviewTestCaseUsers data, @Param("example") TestCaseReviewTestCaseUsersExample example);

}
