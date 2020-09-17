package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.base.domain.TestCaseCommentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseCommentMapper {
    long countByExample(TestCaseCommentExample example);

    int deleteByExample(TestCaseCommentExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseComment record);

    int insertSelective(TestCaseComment record);

    List<TestCaseComment> selectByExampleWithBLOBs(TestCaseCommentExample example);

    List<TestCaseComment> selectByExample(TestCaseCommentExample example);

    TestCaseComment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseComment record, @Param("example") TestCaseCommentExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseComment record, @Param("example") TestCaseCommentExample example);

    int updateByExample(@Param("record") TestCaseComment record, @Param("example") TestCaseCommentExample example);

    int updateByPrimaryKeySelective(TestCaseComment record);

    int updateByPrimaryKeyWithBLOBs(TestCaseComment record);

    int updateByPrimaryKey(TestCaseComment record);
}