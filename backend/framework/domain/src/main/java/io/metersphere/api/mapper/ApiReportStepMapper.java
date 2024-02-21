package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReportStep;
import io.metersphere.api.domain.ApiReportStepExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportStepMapper {
    long countByExample(ApiReportStepExample example);

    int deleteByExample(ApiReportStepExample example);

    int deleteByPrimaryKey(@Param("stepId") String stepId, @Param("reportId") String reportId);

    int insert(ApiReportStep record);

    int insertSelective(ApiReportStep record);

    List<ApiReportStep> selectByExample(ApiReportStepExample example);

    ApiReportStep selectByPrimaryKey(@Param("stepId") String stepId, @Param("reportId") String reportId);

    int updateByExampleSelective(@Param("record") ApiReportStep record, @Param("example") ApiReportStepExample example);

    int updateByExample(@Param("record") ApiReportStep record, @Param("example") ApiReportStepExample example);

    int updateByPrimaryKeySelective(ApiReportStep record);

    int updateByPrimaryKey(ApiReportStep record);

    int batchInsert(@Param("list") List<ApiReportStep> list);

    int batchInsertSelective(@Param("list") List<ApiReportStep> list, @Param("selective") ApiReportStep.Column ... selective);
}