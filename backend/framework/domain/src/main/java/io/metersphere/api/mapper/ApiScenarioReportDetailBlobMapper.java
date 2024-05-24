package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReportDetailBlob;
import io.metersphere.api.domain.ApiScenarioReportDetailBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportDetailBlobMapper {
    long countByExample(ApiScenarioReportDetailBlobExample example);

    int deleteByExample(ApiScenarioReportDetailBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportDetailBlob record);

    int insertSelective(ApiScenarioReportDetailBlob record);

    List<ApiScenarioReportDetailBlob> selectByExampleWithBLOBs(ApiScenarioReportDetailBlobExample example);

    List<ApiScenarioReportDetailBlob> selectByExample(ApiScenarioReportDetailBlobExample example);

    ApiScenarioReportDetailBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportDetailBlob record, @Param("example") ApiScenarioReportDetailBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportDetailBlob record, @Param("example") ApiScenarioReportDetailBlobExample example);

    int updateByExample(@Param("record") ApiScenarioReportDetailBlob record, @Param("example") ApiScenarioReportDetailBlobExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportDetailBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportDetailBlob record);

    int updateByPrimaryKey(ApiScenarioReportDetailBlob record);

    int batchInsert(@Param("list") List<ApiScenarioReportDetailBlob> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReportDetailBlob> list, @Param("selective") ApiScenarioReportDetailBlob.Column ... selective);
}