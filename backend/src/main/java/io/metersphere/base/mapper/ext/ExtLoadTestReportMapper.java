package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.ReportDTO;
import io.metersphere.performance.controller.request.ReportRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestReportMapper {

    List<ReportDTO> getReportList(@Param("reportRequest") ReportRequest request);

    ReportDTO getReportTestAndProInfo(@Param("id") String id);

    List<DashboardTestDTO> selectDashboardTests(@Param("workspaceId") String workspaceId, @Param("startTimestamp") long startTimestamp);

    List<String> selectResourceId(@Param("reportId") String reportId);

    void updateJmxContentIfAbsent(LoadTestReportWithBLOBs record);
}
