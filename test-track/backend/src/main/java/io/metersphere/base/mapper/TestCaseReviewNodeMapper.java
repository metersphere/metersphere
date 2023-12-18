package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseReviewNode;
import io.metersphere.base.domain.TestCaseReviewNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewNodeMapper {
    long countByExample(TestCaseReviewNodeExample example);

    int deleteByExample(TestCaseReviewNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReviewNode record);

    int insertSelective(TestCaseReviewNode record);

    List<TestCaseReviewNode> selectByExample(TestCaseReviewNodeExample example);

    TestCaseReviewNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReviewNode record, @Param("example") TestCaseReviewNodeExample example);

    int updateByExample(@Param("record") TestCaseReviewNode record, @Param("example") TestCaseReviewNodeExample example);

    int updateByPrimaryKeySelective(TestCaseReviewNode record);

    int updateByPrimaryKey(TestCaseReviewNode record);
}