package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanUiScenario;
import io.metersphere.plan.domain.TestPlanUiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanUiScenarioMapper {
    long countByExample(TestPlanUiScenarioExample example);

    int deleteByExample(TestPlanUiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanUiScenario record);

    int insertSelective(TestPlanUiScenario record);

    List<TestPlanUiScenario> selectByExampleWithBLOBs(TestPlanUiScenarioExample example);

    List<TestPlanUiScenario> selectByExample(TestPlanUiScenarioExample example);

    TestPlanUiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanUiScenario record, @Param("example") TestPlanUiScenarioExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanUiScenario record, @Param("example") TestPlanUiScenarioExample example);

    int updateByExample(@Param("record") TestPlanUiScenario record, @Param("example") TestPlanUiScenarioExample example);

    int updateByPrimaryKeySelective(TestPlanUiScenario record);

    int updateByPrimaryKeyWithBLOBs(TestPlanUiScenario record);

    int updateByPrimaryKey(TestPlanUiScenario record);
}