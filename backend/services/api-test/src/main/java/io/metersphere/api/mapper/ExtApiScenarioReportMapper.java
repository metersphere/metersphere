package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.scenario.ApiScenarioReportStepDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportMapper {
    List<ApiScenarioReport> list(@Param("request") ApiReportPageRequest request);

    List<String> getIds(@Param("request") ApiReportBatchRequest request);

    List<ApiScenarioReport> selectApiReportByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiScenarioReportStepDTO> selectStepByReportId(@Param("reportId") String reportId);
}
