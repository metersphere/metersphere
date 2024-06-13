package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.domain.TestPlanReportApiScenarioExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanReportApiScenarioMapper {
    long countByExample(TestPlanReportApiScenarioExample example);

    int deleteByExample(TestPlanReportApiScenarioExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportApiScenario record);

    int insertSelective(TestPlanReportApiScenario record);

    List<TestPlanReportApiScenario> selectByExample(TestPlanReportApiScenarioExample example);

    TestPlanReportApiScenario selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportApiScenario record, @Param("example") TestPlanReportApiScenarioExample example);

    int updateByExample(@Param("record") TestPlanReportApiScenario record, @Param("example") TestPlanReportApiScenarioExample example);

    int updateByPrimaryKeySelective(TestPlanReportApiScenario record);

    int updateByPrimaryKey(TestPlanReportApiScenario record);

    int batchInsert(@Param("list") List<TestPlanReportApiScenario> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportApiScenario> list, @Param("selective") TestPlanReportApiScenario.Column ... selective);
}