package io.metersphere.api.mapper;

import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportDetailBlobMapper {

    List<ApiScenarioReportDetailDTO> selectByExampleWithBLOBs(@Param("stepId") String stepId, @Param("reportId") String reportId);

}