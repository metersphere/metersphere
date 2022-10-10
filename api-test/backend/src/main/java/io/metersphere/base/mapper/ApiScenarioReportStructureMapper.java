package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiScenarioReportStructure;
import io.metersphere.base.domain.ApiScenarioReportStructureExample;
import io.metersphere.base.domain.ApiScenarioReportStructureWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiScenarioReportStructureMapper {
    long countByExample(ApiScenarioReportStructureExample example);

    int deleteByExample(ApiScenarioReportStructureExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportStructureWithBLOBs record);

    int insertSelective(ApiScenarioReportStructureWithBLOBs record);

    List<ApiScenarioReportStructureWithBLOBs> selectByExampleWithBLOBs(ApiScenarioReportStructureExample example);

    List<ApiScenarioReportStructure> selectByExample(ApiScenarioReportStructureExample example);

    ApiScenarioReportStructureWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportStructureWithBLOBs record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiScenarioReportStructureWithBLOBs record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByExample(@Param("record") ApiScenarioReportStructure record, @Param("example") ApiScenarioReportStructureExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportStructureWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ApiScenarioReportStructureWithBLOBs record);

    int updateByPrimaryKey(ApiScenarioReportStructure record);
}