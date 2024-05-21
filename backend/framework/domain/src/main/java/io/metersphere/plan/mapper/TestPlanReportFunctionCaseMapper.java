package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportFunctionCase;
import io.metersphere.plan.domain.TestPlanReportFunctionCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanReportFunctionCaseMapper {
    long countByExample(TestPlanReportFunctionCaseExample example);

    int deleteByExample(TestPlanReportFunctionCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportFunctionCase record);

    int insertSelective(TestPlanReportFunctionCase record);

    List<TestPlanReportFunctionCase> selectByExample(TestPlanReportFunctionCaseExample example);

    TestPlanReportFunctionCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportFunctionCase record, @Param("example") TestPlanReportFunctionCaseExample example);

    int updateByExample(@Param("record") TestPlanReportFunctionCase record, @Param("example") TestPlanReportFunctionCaseExample example);

    int updateByPrimaryKeySelective(TestPlanReportFunctionCase record);

    int updateByPrimaryKey(TestPlanReportFunctionCase record);

    int batchInsert(@Param("list") List<TestPlanReportFunctionCase> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportFunctionCase> list, @Param("selective") TestPlanReportFunctionCase.Column ... selective);
}