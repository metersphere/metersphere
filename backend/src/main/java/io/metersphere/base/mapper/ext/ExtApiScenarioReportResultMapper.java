package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;

import java.util.List;

public interface ExtApiScenarioReportResultMapper {
    List<ApiScenarioReportResultWithBLOBs> selectBaseInfoResultByReportId(String reportId);
}
