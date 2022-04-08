package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiScenarioReportResult;

import java.util.List;

public interface ExtApiScenarioReportResultMapper {
    List<ApiScenarioReportResult> selectBaseInfoResultByReportId(String reportId);
}
