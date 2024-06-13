package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiCaseExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanReportApiCaseMapper {
    long countByExample(TestPlanReportApiCaseExample example);

    int deleteByExample(TestPlanReportApiCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportApiCase record);

    int insertSelective(TestPlanReportApiCase record);

    List<TestPlanReportApiCase> selectByExample(TestPlanReportApiCaseExample example);

    TestPlanReportApiCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportApiCase record, @Param("example") TestPlanReportApiCaseExample example);

    int updateByExample(@Param("record") TestPlanReportApiCase record, @Param("example") TestPlanReportApiCaseExample example);

    int updateByPrimaryKeySelective(TestPlanReportApiCase record);

    int updateByPrimaryKey(TestPlanReportApiCase record);

    int batchInsert(@Param("list") List<TestPlanReportApiCase> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportApiCase> list, @Param("selective") TestPlanReportApiCase.Column ... selective);
}