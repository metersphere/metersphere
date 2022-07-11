package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.ReportDTO;
import io.metersphere.performance.controller.request.ReportRequest;
import io.metersphere.track.dto.PlanReportCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestReportMapper {

    List<ReportDTO> getReportList(@Param("reportRequest") ReportRequest request);

    ReportDTO getReportTestAndProInfo(@Param("id") String id);

    List<DashboardTestDTO> selectDashboardTests(@Param("workspaceId") String workspaceId, @Param("startTimestamp") long startTimestamp);

    List<String> selectResourceId(@Param("reportId") String reportId);

    void updateJmxContentIfAbsent(LoadTestReportWithBLOBs record);

    List<LoadTestReport> selectReportByProjectId(String projectId);

    List<PlanReportCaseDTO> selectForPlanReport(@Param("ids") List<String> reportIds);

    int updateReportVumStatus(String reportId, String reportKey, String nextStatus, String preStatus);

    List<FileMetadata> getFileMetadataById(@Param("reportId") String reportId);

    List<String> selectReportIdByTestId(@Param("testId") String testId);

    String selectStatusById(String id);

}
