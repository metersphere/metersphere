package io.metersphere.api.mapper;

import io.metersphere.api.dto.scenario.ApiScenarioReportDetailBlobDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportDetailBlobMapper {

    List<ApiScenarioReportDetailBlobDTO> selectByExampleWithBLOBs(@Param("stepId") String stepId, @Param("reportId") String reportId);

}