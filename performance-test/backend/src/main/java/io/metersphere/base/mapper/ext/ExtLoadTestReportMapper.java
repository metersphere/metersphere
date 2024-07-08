package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.ReportDTO;
import io.metersphere.request.ReportRequest;
import io.metersphere.task.dto.TaskRequestDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtLoadTestReportMapper {

    List<ReportDTO> getReportList(@Param("reportRequest") ReportRequest request);

    ReportDTO getReportTestAndProInfo(@Param("id") String id);

    List<DashboardTestDTO> selectDashboardTests(@Param("projectId") String projectId, @Param("startTimestamp") long startTimestamp);

    List<String> selectResourceId(@Param("reportId") String reportId);

    void updateJmxContentIfAbsent(LoadTestReportWithBLOBs record);

    List<LoadTestReport> selectReportByProjectId(@Param("request") TaskRequestDTO request);

    int updateReportVumStatus(String reportId, String reportKey, String nextStatus, String preStatus);

    List<FileMetadata> getFileMetadataById(@Param("reportId") String reportId);

    List<String> selectReportIdByTestId(@Param("testId") String testId);

    String selectStatusById(String id);

    List<String> getStatusByIds(@Param("ids") List<String> reportIds);
}
