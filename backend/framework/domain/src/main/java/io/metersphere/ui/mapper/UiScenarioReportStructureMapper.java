package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReportStructure;
import io.metersphere.ui.domain.UiScenarioReportStructureExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportStructureMapper {
    long countByExample(UiScenarioReportStructureExample example);

    int deleteByExample(UiScenarioReportStructureExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportStructure record);

    int insertSelective(UiScenarioReportStructure record);

    List<UiScenarioReportStructure> selectByExampleWithBLOBs(UiScenarioReportStructureExample example);

    List<UiScenarioReportStructure> selectByExample(UiScenarioReportStructureExample example);

    UiScenarioReportStructure selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportStructure record, @Param("example") UiScenarioReportStructureExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportStructure record, @Param("example") UiScenarioReportStructureExample example);

    int updateByExample(@Param("record") UiScenarioReportStructure record, @Param("example") UiScenarioReportStructureExample example);

    int updateByPrimaryKeySelective(UiScenarioReportStructure record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportStructure record);

    int updateByPrimaryKey(UiScenarioReportStructure record);
}