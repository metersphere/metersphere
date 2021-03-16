package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewLoad;
import io.metersphere.base.domain.TestCaseReviewLoadExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewLoadMapper {
    long countByExample(TestCaseReviewLoadExample example);

    int deleteByExample(TestCaseReviewLoadExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReviewLoad record);

    int insertSelective(TestCaseReviewLoad record);

    List<TestCaseReviewLoad> selectByExample(TestCaseReviewLoadExample example);

    TestCaseReviewLoad selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReviewLoad record, @Param("example") TestCaseReviewLoadExample example);

    int updateByExample(@Param("record") TestCaseReviewLoad record, @Param("example") TestCaseReviewLoadExample example);

    int updateByPrimaryKeySelective(TestCaseReviewLoad record);

    int updateByPrimaryKey(TestCaseReviewLoad record);
}