package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportContent;
import io.metersphere.plan.domain.TestPlanReportContentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanReportContentMapper {
    long countByExample(TestPlanReportContentExample example);

    int deleteByExample(TestPlanReportContentExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportContent record);

    int insertSelective(TestPlanReportContent record);

    List<TestPlanReportContent> selectByExampleWithBLOBs(TestPlanReportContentExample example);

    List<TestPlanReportContent> selectByExample(TestPlanReportContentExample example);

    TestPlanReportContent selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportContent record, @Param("example") TestPlanReportContentExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanReportContent record, @Param("example") TestPlanReportContentExample example);

    int updateByExample(@Param("record") TestPlanReportContent record, @Param("example") TestPlanReportContentExample example);

    int updateByPrimaryKeySelective(TestPlanReportContent record);

    int updateByPrimaryKeyWithBLOBs(TestPlanReportContent record);

    int updateByPrimaryKey(TestPlanReportContent record);
}