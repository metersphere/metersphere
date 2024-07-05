package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportComponent;
import io.metersphere.plan.domain.TestPlanReportComponentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPlanReportComponentMapper {
    long countByExample(TestPlanReportComponentExample example);

    int deleteByExample(TestPlanReportComponentExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportComponent record);

    int insertSelective(TestPlanReportComponent record);

    List<TestPlanReportComponent> selectByExampleWithBLOBs(TestPlanReportComponentExample example);

    List<TestPlanReportComponent> selectByExample(TestPlanReportComponentExample example);

    TestPlanReportComponent selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportComponent record, @Param("example") TestPlanReportComponentExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanReportComponent record, @Param("example") TestPlanReportComponentExample example);

    int updateByExample(@Param("record") TestPlanReportComponent record, @Param("example") TestPlanReportComponentExample example);

    int updateByPrimaryKeySelective(TestPlanReportComponent record);

    int updateByPrimaryKeyWithBLOBs(TestPlanReportComponent record);

    int updateByPrimaryKey(TestPlanReportComponent record);

    int batchInsert(@Param("list") List<TestPlanReportComponent> list);

    int batchInsertSelective(@Param("list") List<TestPlanReportComponent> list, @Param("selective") TestPlanReportComponent.Column ... selective);
}