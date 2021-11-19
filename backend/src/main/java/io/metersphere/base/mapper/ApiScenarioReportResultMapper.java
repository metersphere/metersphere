package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.domain.ApiScenarioReportResultExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportResultMapper {
    long countByExample(ApiScenarioReportResultExample example);

    int deleteByExample(ApiScenarioReportResultExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportResult record);

    int insertSelective(ApiScenarioReportResult record);

    List<ApiScenarioReportResult> selectByExampleWithBLOBs(ApiScenarioReportResultExample example);

    List<ApiScenarioReportResult> selectByExample(ApiScenarioReportResultExample example);

    ApiScenarioReportResult selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportResult record, @Param("example") ApiScenarioReportResultExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportResult record, @Param("example") ApiScenarioReportResultExample example);

    int updateByExample(@Param("record") ApiScenarioReportResult record, @Param("example") ApiScenarioReportResultExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportResult record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportResult record);

    int updateByPrimaryKey(ApiScenarioReportResult record);
}