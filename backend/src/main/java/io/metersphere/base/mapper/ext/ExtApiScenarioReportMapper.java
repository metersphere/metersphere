package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportMapper {
    List<APIScenarioReportResult> list(@Param("request") QueryAPIReportRequest request);

    APIReportResult get(@Param("reportId") String reportId);

}