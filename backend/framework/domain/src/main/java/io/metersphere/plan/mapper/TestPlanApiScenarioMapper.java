package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanApiScenarioMapper {
    long countByExample(TestPlanApiScenarioExample example);

    int deleteByExample(TestPlanApiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanApiScenario record);

    int insertSelective(TestPlanApiScenario record);

    List<TestPlanApiScenario> selectByExample(TestPlanApiScenarioExample example);

    TestPlanApiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanApiScenario record, @Param("example") TestPlanApiScenarioExample example);

    int updateByExample(@Param("record") TestPlanApiScenario record, @Param("example") TestPlanApiScenarioExample example);

    int updateByPrimaryKeySelective(TestPlanApiScenario record);

    int updateByPrimaryKey(TestPlanApiScenario record);

    int batchInsert(@Param("list") List<TestPlanApiScenario> list);

    int batchInsertSelective(@Param("list") List<TestPlanApiScenario> list, @Param("selective") TestPlanApiScenario.Column ... selective);
}