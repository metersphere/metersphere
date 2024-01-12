package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanMapper {
    long countByExample(TestPlanExample example);

    int deleteByExample(TestPlanExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlan record);

    int insertSelective(TestPlan record);

    List<TestPlan> selectByExample(TestPlanExample example);

    TestPlan selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlan record, @Param("example") TestPlanExample example);

    int updateByExample(@Param("record") TestPlan record, @Param("example") TestPlanExample example);

    int updateByPrimaryKeySelective(TestPlan record);

    int updateByPrimaryKey(TestPlan record);

    int batchInsert(@Param("list") List<TestPlan> list);

    int batchInsertSelective(@Param("list") List<TestPlan> list, @Param("selective") TestPlan.Column ... selective);
}