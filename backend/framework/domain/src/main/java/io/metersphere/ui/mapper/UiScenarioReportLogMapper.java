package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReportLog;
import io.metersphere.ui.domain.UiScenarioReportLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportLogMapper {
    long countByExample(UiScenarioReportLogExample example);

    int deleteByExample(UiScenarioReportLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportLog record);

    int insertSelective(UiScenarioReportLog record);

    List<UiScenarioReportLog> selectByExampleWithBLOBs(UiScenarioReportLogExample example);

    List<UiScenarioReportLog> selectByExample(UiScenarioReportLogExample example);

    UiScenarioReportLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportLog record, @Param("example") UiScenarioReportLogExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportLog record, @Param("example") UiScenarioReportLogExample example);

    int updateByExample(@Param("record") UiScenarioReportLog record, @Param("example") UiScenarioReportLogExample example);

    int updateByPrimaryKeySelective(UiScenarioReportLog record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportLog record);

    int updateByPrimaryKey(UiScenarioReportLog record);
}