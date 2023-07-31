package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReportEnvironment;
import io.metersphere.ui.domain.UiScenarioReportEnvironmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportEnvironmentMapper {
    long countByExample(UiScenarioReportEnvironmentExample example);

    int deleteByExample(UiScenarioReportEnvironmentExample example);

    int deleteByPrimaryKey(String reportId);

    int insert(UiScenarioReportEnvironment record);

    int insertSelective(UiScenarioReportEnvironment record);

    List<UiScenarioReportEnvironment> selectByExample(UiScenarioReportEnvironmentExample example);

    UiScenarioReportEnvironment selectByPrimaryKey(String reportId);

    int updateByExampleSelective(@Param("record") UiScenarioReportEnvironment record, @Param("example") UiScenarioReportEnvironmentExample example);

    int updateByExample(@Param("record") UiScenarioReportEnvironment record, @Param("example") UiScenarioReportEnvironmentExample example);

    int updateByPrimaryKeySelective(UiScenarioReportEnvironment record);

    int updateByPrimaryKey(UiScenarioReportEnvironment record);

    int batchInsert(@Param("list") List<UiScenarioReportEnvironment> list);

    int batchInsertSelective(@Param("list") List<UiScenarioReportEnvironment> list, @Param("selective") UiScenarioReportEnvironment.Column ... selective);
}