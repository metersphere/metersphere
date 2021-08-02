package io.metersphere.base.mapper;

import io.metersphere.base.domain.TestPlanReportResource;
import io.metersphere.base.domain.TestPlanReportResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanReportResourceMapper {
    long countByExample(TestPlanReportResourceExample example);

    int deleteByExample(TestPlanReportResourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanReportResource record);

    int insertSelective(TestPlanReportResource record);

    List<TestPlanReportResource> selectByExample(TestPlanReportResourceExample example);

    TestPlanReportResource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanReportResource record, @Param("example") TestPlanReportResourceExample example);

    int updateByExample(@Param("record") TestPlanReportResource record, @Param("example") TestPlanReportResourceExample example);

    int updateByPrimaryKeySelective(TestPlanReportResource record);

    int updateByPrimaryKey(TestPlanReportResource record);
}