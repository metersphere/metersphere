package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReportDetail;
import io.metersphere.api.domain.ApiScenarioReportDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportDetailMapper {
    long countByExample(ApiScenarioReportDetailExample example);

    int deleteByExample(ApiScenarioReportDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiScenarioReportDetail record);

    int insertSelective(ApiScenarioReportDetail record);

    List<ApiScenarioReportDetail> selectByExample(ApiScenarioReportDetailExample example);

    ApiScenarioReportDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiScenarioReportDetail record, @Param("example") ApiScenarioReportDetailExample example);

    int updateByExample(@Param("record") ApiScenarioReportDetail record, @Param("example") ApiScenarioReportDetailExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportDetail record);

    int updateByPrimaryKey(ApiScenarioReportDetail record);

    int batchInsert(@Param("list") List<ApiScenarioReportDetail> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReportDetail> list, @Param("selective") ApiScenarioReportDetail.Column ... selective);
}