package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanExample;
import io.metersphere.base.domain.TestPlanWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanMapper {
    long countByExample(TestPlanExample example);

    int deleteByExample(TestPlanExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanWithBLOBs record);

    int insertSelective(TestPlanWithBLOBs record);

    List<TestPlanWithBLOBs> selectByExampleWithBLOBs(TestPlanExample example);

    List<TestPlan> selectByExample(TestPlanExample example);

    TestPlanWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanWithBLOBs record, @Param("example") TestPlanExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanWithBLOBs record, @Param("example") TestPlanExample example);

    int updateByExample(@Param("record") TestPlan record, @Param("example") TestPlanExample example);

    int updateByPrimaryKeySelective(TestPlanWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(TestPlanWithBLOBs record);

    int updateByPrimaryKey(TestPlan record);
}