package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReportStructure;
import io.metersphere.api.domain.ApiScenarioReportStructureExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportStructureMapper {
    long countByExample(ApiScenarioReportStructureExample example);

    int deleteByExample(ApiScenarioReportStructureExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(ApiScenarioReportStructure record);

    int insertSelective(ApiScenarioReportStructure record);

    List<ApiScenarioReportStructure> selectByExampleWithBLOBs(ApiScenarioReportStructureExample example);

    List<ApiScenarioReportStructure> selectByExample(ApiScenarioReportStructureExample example);

    ApiScenarioReportStructure selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") ApiScenarioReportStructure record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportStructure record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByExample(@Param("record") ApiScenarioReportStructure record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportStructure record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportStructure record);

    int batchInsert(@Param("list") List<ApiScenarioReportStructure> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReportStructure> list, @Param("selective") ApiScenarioReportStructure.Column ... selective);
}