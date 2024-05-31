package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanAllocationType;
import io.metersphere.plan.domain.TestPlanAllocationTypeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanAllocationTypeMapper {
    long countByExample(TestPlanAllocationTypeExample example);

    int deleteByExample(TestPlanAllocationTypeExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanAllocationType record);

    int insertSelective(TestPlanAllocationType record);

    List<TestPlanAllocationType> selectByExample(TestPlanAllocationTypeExample example);

    TestPlanAllocationType selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanAllocationType record, @Param("example") TestPlanAllocationTypeExample example);

    int updateByExample(@Param("record") TestPlanAllocationType record, @Param("example") TestPlanAllocationTypeExample example);

    int updateByPrimaryKeySelective(TestPlanAllocationType record);

    int updateByPrimaryKey(TestPlanAllocationType record);

    int batchInsert(@Param("list") List<TestPlanAllocationType> list);

    int batchInsertSelective(@Param("list") List<TestPlanAllocationType> list, @Param("selective") TestPlanAllocationType.Column ... selective);
}