package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReportLog;
import io.metersphere.api.domain.ApiScenarioReportLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportLogMapper {
    long countByExample(ApiScenarioReportLogExample example);

    int deleteByExample(ApiScenarioReportLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportLog record);

    int insertSelective(ApiScenarioReportLog record);

    List<ApiScenarioReportLog> selectByExampleWithBLOBs(ApiScenarioReportLogExample example);

    List<ApiScenarioReportLog> selectByExample(ApiScenarioReportLogExample example);

    ApiScenarioReportLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportLog record, @Param("example") ApiScenarioReportLogExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportLog record, @Param("example") ApiScenarioReportLogExample example);

    int updateByExample(@Param("record") ApiScenarioReportLog record, @Param("example") ApiScenarioReportLogExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportLog record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportLog record);

    int updateByPrimaryKey(ApiScenarioReportLog record);

    int batchInsert(@Param("list") List<ApiScenarioReportLog> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReportLog> list, @Param("selective") ApiScenarioReportLog.Column ... selective);
}