package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanRecordApiScenarioInfo;
import io.metersphere.plan.domain.TestPlanRecordApiScenarioInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanRecordApiScenarioInfoMapper {
    long countByExample(TestPlanRecordApiScenarioInfoExample example);

    int deleteByExample(TestPlanRecordApiScenarioInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanRecordApiScenarioInfo record);

    int insertSelective(TestPlanRecordApiScenarioInfo record);

    List<TestPlanRecordApiScenarioInfo> selectByExample(TestPlanRecordApiScenarioInfoExample example);

    TestPlanRecordApiScenarioInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanRecordApiScenarioInfo record, @Param("example") TestPlanRecordApiScenarioInfoExample example);

    int updateByExample(@Param("record") TestPlanRecordApiScenarioInfo record, @Param("example") TestPlanRecordApiScenarioInfoExample example);

    int updateByPrimaryKeySelective(TestPlanRecordApiScenarioInfo record);

    int updateByPrimaryKey(TestPlanRecordApiScenarioInfo record);
}