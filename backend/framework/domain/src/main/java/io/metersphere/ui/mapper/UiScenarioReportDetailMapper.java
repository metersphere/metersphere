package io.metersphere.ui.mapper;

import io.metersphere.ui.domain.UiScenarioReportDetail;
import io.metersphere.ui.domain.UiScenarioReportDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UiScenarioReportDetailMapper {
    long countByExample(UiScenarioReportDetailExample example);

    int deleteByExample(UiScenarioReportDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(UiScenarioReportDetail record);

    int insertSelective(UiScenarioReportDetail record);

    List<UiScenarioReportDetail> selectByExampleWithBLOBs(UiScenarioReportDetailExample example);

    List<UiScenarioReportDetail> selectByExample(UiScenarioReportDetailExample example);

    UiScenarioReportDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByExample(@Param("record") UiScenarioReportDetail record, @Param("example") UiScenarioReportDetailExample example);

    int updateByPrimaryKeySelective(UiScenarioReportDetail record);

    int updateByPrimaryKeyWithBLOBs(UiScenarioReportDetail record);

    int updateByPrimaryKey(UiScenarioReportDetail record);

    int batchInsert(@Param("list") List<UiScenarioReportDetail> list);

    int batchInsertSelective(@Param("list") List<UiScenarioReportDetail> list, @Param("selective") UiScenarioReportDetail.Column ... selective);
}