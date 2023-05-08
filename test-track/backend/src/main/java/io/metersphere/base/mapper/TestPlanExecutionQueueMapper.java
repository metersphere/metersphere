package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanExecutionQueue;
import io.metersphere.base.domain.TestPlanExecutionQueueExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanExecutionQueueMapper {
    long countByExample(TestPlanExecutionQueueExample example);

    int deleteByExample(TestPlanExecutionQueueExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanExecutionQueue record);

    int insertSelective(TestPlanExecutionQueue record);

    List<TestPlanExecutionQueue> selectByExample(TestPlanExecutionQueueExample example);

    TestPlanExecutionQueue selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanExecutionQueue record, @Param("example") TestPlanExecutionQueueExample example);

    int updateByExample(@Param("record") TestPlanExecutionQueue record, @Param("example") TestPlanExecutionQueueExample example);

    int updateByPrimaryKeySelective(TestPlanExecutionQueue record);

    int updateByPrimaryKey(TestPlanExecutionQueue record);
}