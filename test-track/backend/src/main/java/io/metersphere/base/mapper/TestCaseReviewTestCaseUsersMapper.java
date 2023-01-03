package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewTestCaseUsers;
import io.metersphere.base.domain.TestCaseReviewTestCaseUsersExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestCaseReviewTestCaseUsersMapper {

    long countByExample(TestCaseReviewTestCaseUsersExample example);

    int deleteByExample(TestCaseReviewTestCaseUsersExample example);

    int insert(TestCaseReviewTestCaseUsers record);

    int insertSelective(TestCaseReviewTestCaseUsers record);

    List<TestCaseReviewTestCaseUsers> selectByExample(TestCaseReviewTestCaseUsersExample example);

    int updateByExampleSelective(@Param("record") TestCaseReviewTestCaseUsers record, @Param("example") TestCaseReviewTestCaseUsersExample example);

    int updateByExample(@Param("record") TestCaseReviewTestCaseUsers record, @Param("example") TestCaseReviewTestCaseUsersExample example);

}
