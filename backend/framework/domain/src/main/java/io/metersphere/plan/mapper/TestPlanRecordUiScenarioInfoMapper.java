package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanRecordUiScenarioInfo;
import io.metersphere.plan.domain.TestPlanRecordUiScenarioInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanRecordUiScenarioInfoMapper {
    long countByExample(TestPlanRecordUiScenarioInfoExample example);

    int deleteByExample(TestPlanRecordUiScenarioInfoExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanRecordUiScenarioInfo record);

    int insertSelective(TestPlanRecordUiScenarioInfo record);

    List<TestPlanRecordUiScenarioInfo> selectByExample(TestPlanRecordUiScenarioInfoExample example);

    TestPlanRecordUiScenarioInfo selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanRecordUiScenarioInfo record, @Param("example") TestPlanRecordUiScenarioInfoExample example);

    int updateByExample(@Param("record") TestPlanRecordUiScenarioInfo record, @Param("example") TestPlanRecordUiScenarioInfoExample example);

    int updateByPrimaryKeySelective(TestPlanRecordUiScenarioInfo record);

    int updateByPrimaryKey(TestPlanRecordUiScenarioInfo record);
}