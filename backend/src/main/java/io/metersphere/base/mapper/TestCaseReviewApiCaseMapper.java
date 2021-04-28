package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewApiCase;
import io.metersphere.base.domain.TestCaseReviewApiCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewApiCaseMapper {
    long countByExample(TestCaseReviewApiCaseExample example);

    int deleteByExample(TestCaseReviewApiCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReviewApiCase record);

    int insertSelective(TestCaseReviewApiCase record);

    List<TestCaseReviewApiCase> selectByExample(TestCaseReviewApiCaseExample example);

    TestCaseReviewApiCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReviewApiCase record, @Param("example") TestCaseReviewApiCaseExample example);

    int updateByExample(@Param("record") TestCaseReviewApiCase record, @Param("example") TestCaseReviewApiCaseExample example);

    int updateByPrimaryKeySelective(TestCaseReviewApiCase record);

    int updateByPrimaryKey(TestCaseReviewApiCase record);
}