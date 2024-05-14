package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportSummary;
import io.metersphere.plan.domain.TestPlanReportSummaryExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanReportSummaryMapper {
    long countByExample(TestPlanReportSummaryExample example);

    int deleteByExample(TestPlanReportSummaryExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportSummary record);

    int insertSelective(TestPlanReportSummary record);

    List<TestPlanReportSummary> selectByExampleWithBLOBs(TestPlanReportSummaryExample example);

    List<TestPlanReportSummary> selectByExample(TestPlanReportSummaryExample example);

    TestPlanReportSummary selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportSummary record, @Param("example") TestPlanReportSummaryExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanReportSummary record, @Param("example") TestPlanReportSummaryExample example);

    int updateByExample(@Param("record") TestPlanReportSummary record, @Param("example") TestPlanReportSummaryExample example);

    int updateByPrimaryKeySelective(TestPlanReportSummary record);

    int updateByPrimaryKeyWithBLOBs(TestPlanReportSummary record);

    int updateByPrimaryKey(TestPlanReportSummary record);

    int batchInsert(@Param("list") List<TestPlanReportSummary> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportSummary> list, @Param("selective") TestPlanReportSummary.Column ... selective);
}