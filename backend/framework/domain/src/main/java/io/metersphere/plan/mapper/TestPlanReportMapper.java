package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.domain.TestPlanReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanReportMapper {
    long countByExample(TestPlanReportExample example);

    int deleteByExample(TestPlanReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReport record);

    int insertSelective(TestPlanReport record);

    List<TestPlanReport> selectByExample(TestPlanReportExample example);

    TestPlanReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReport record, @Param("example") TestPlanReportExample example);

    int updateByExample(@Param("record") TestPlanReport record, @Param("example") TestPlanReportExample example);

    int updateByPrimaryKeySelective(TestPlanReport record);

    int updateByPrimaryKey(TestPlanReport record);
}