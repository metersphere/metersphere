package io.metersphere.base.mapper;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestReportMapper {
    long countByExample(LoadTestReportExample example);

    int deleteByExample(LoadTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestReport record);

    int insertSelective(LoadTestReport record);

    List<LoadTestReport> selectByExample(LoadTestReportExample example);

    LoadTestReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByExample(@Param("record") LoadTestReport record, @Param("example") LoadTestReportExample example);

    int updateByPrimaryKeySelective(LoadTestReport record);

    int updateByPrimaryKey(LoadTestReport record);
}