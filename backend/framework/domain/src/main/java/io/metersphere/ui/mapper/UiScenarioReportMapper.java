package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReport;
import io.metersphere.ui.domain.UiScenarioReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportMapper {
    long countByExample(UiScenarioReportExample example);

    int deleteByExample(UiScenarioReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReport record);

    int insertSelective(UiScenarioReport record);

    List<UiScenarioReport> selectByExample(UiScenarioReportExample example);

    UiScenarioReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReport record, @Param("example") UiScenarioReportExample example);

    int updateByExample(@Param("record") UiScenarioReport record, @Param("example") UiScenarioReportExample example);

    int updateByPrimaryKeySelective(UiScenarioReport record);

    int updateByPrimaryKey(UiScenarioReport record);
}