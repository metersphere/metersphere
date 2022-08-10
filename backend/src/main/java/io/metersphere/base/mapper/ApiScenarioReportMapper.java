package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportExample;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportMapper {
    long countByExample(ApiScenarioReportExample example);

    int deleteByExample(ApiScenarioReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportWithBLOBs record);

    int insertSelective(ApiScenarioReportWithBLOBs record);

    List<ApiScenarioReportWithBLOBs> selectByExampleWithBLOBs(ApiScenarioReportExample example);

    List<ApiScenarioReport> selectByExample(ApiScenarioReportExample example);

    ApiScenarioReportWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportWithBLOBs record, @Param("example") ApiScenarioReportExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportWithBLOBs record, @Param("example") ApiScenarioReportExample example);

    int updateByExample(@Param("record") ApiScenarioReport record, @Param("example") ApiScenarioReportExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportWithBLOBs record);

    int updateByPrimaryKey(ApiScenarioReport record);
}