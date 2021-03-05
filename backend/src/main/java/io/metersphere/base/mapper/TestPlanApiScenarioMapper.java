package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.base.domain.TestPlanApiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanApiScenarioMapper {
    long countByExample(TestPlanApiScenarioExample example);

    int deleteByExample(TestPlanApiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanApiScenario record);

    int insertSelective(TestPlanApiScenario record);

    List<TestPlanApiScenario> selectByExampleWithBLOBs(TestPlanApiScenarioExample example);

    List<TestPlanApiScenario> selectByExample(TestPlanApiScenarioExample example);

    TestPlanApiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanApiScenario record, @Param("example") TestPlanApiScenarioExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanApiScenario record, @Param("example") TestPlanApiScenarioExample example);

    int updateByExample(@Param("record") TestPlanApiScenario record, @Param("example") TestPlanApiScenarioExample example);

    int updateByPrimaryKeySelective(TestPlanApiScenario record);

    int updateByPrimaryKeyWithBLOBs(TestPlanApiScenario record);

    int updateByPrimaryKey(TestPlanApiScenario record);
}