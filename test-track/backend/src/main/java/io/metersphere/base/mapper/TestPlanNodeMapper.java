package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanNode;
import io.metersphere.base.domain.TestPlanNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanNodeMapper {
    long countByExample(TestPlanNodeExample example);

    int deleteByExample(TestPlanNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanNode record);

    int insertSelective(TestPlanNode record);

    List<TestPlanNode> selectByExample(TestPlanNodeExample example);

    TestPlanNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanNode record, @Param("example") TestPlanNodeExample example);

    int updateByExample(@Param("record") TestPlanNode record, @Param("example") TestPlanNodeExample example);

    int updateByPrimaryKeySelective(TestPlanNode record);

    int updateByPrimaryKey(TestPlanNode record);
}