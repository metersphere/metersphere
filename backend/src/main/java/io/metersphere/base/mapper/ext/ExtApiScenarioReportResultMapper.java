package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportResultMapper {
    List<ApiScenarioReportResultWithBLOBs> selectBaseInfoResultByReportId(String reportId);

    void deleteHisReportResult(@Param("scenarioIds") List<String> scenarioIds, @Param("reportId") String reportId);

    List<String> getReportIds(@Param("ids") List<String> ids);

    List<String> selectDistinctStatusByReportId(String reportId);
}
