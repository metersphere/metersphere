package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReportStep;
import io.metersphere.api.domain.ApiScenarioReportStepExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiScenarioReportStepMapper {
    long countByExample(ApiScenarioReportStepExample example);

    int deleteByExample(ApiScenarioReportStepExample example);

    int deleteByPrimaryKey(@Param("stepId") String stepId, @Param("reportId") String reportId);

    int insert(ApiScenarioReportStep record);

    int insertSelective(ApiScenarioReportStep record);

    List<ApiScenarioReportStep> selectByExample(ApiScenarioReportStepExample example);

    ApiScenarioReportStep selectByPrimaryKey(@Param("stepId") String stepId, @Param("reportId") String reportId);

    int updateByExampleSelective(@Param("record") ApiScenarioReportStep record, @Param("example") ApiScenarioReportStepExample example);

    int updateByExample(@Param("record") ApiScenarioReportStep record, @Param("example") ApiScenarioReportStepExample example);

    int updateByPrimaryKeySelective(ApiScenarioReportStep record);

    int updateByPrimaryKey(ApiScenarioReportStep record);

    int batchInsert(@Param("list") List<ApiScenarioReportStep> list);

    int batchInsertSelective(@Param("list") List<ApiScenarioReportStep> list, @Param("selective") ApiScenarioReportStep.Column ... selective);
}