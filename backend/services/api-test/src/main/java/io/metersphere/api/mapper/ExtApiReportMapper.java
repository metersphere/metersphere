package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.definition.ApiReportStepDTO;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.system.dto.sdk.ApiReportMessageDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiReportMapper {
    @BaseConditionFilter
    List<ApiReport> list(@Param("request") ApiReportPageRequest request);

    @BaseConditionFilter
    List<String> getIds(@Param("request") ApiReportBatchRequest request);

    List<ApiReport> selectApiReportByIds(@Param("ids") List<String> ids);

    List<ApiReportStepDTO> selectStepsByReportId(String id);

    List<String> selectApiReportByProjectId(String projectId);

    List<String> selectApiReportByProjectIdAndTime(@Param("time") long time, @Param("projectId") String projectId);

    int countApiReportByTime(@Param("time") long time, @Param("projectId") String projectId);

    List<TaskCenterDTO> taskCenterlist(@Param("request") TaskCenterPageRequest request, @Param("projectIds") List<String> projectIds,
                                       @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<ReportDTO> getReports(@Param("request") TaskCenterBatchRequest request, @Param("projectIds") List<String> projectIds,
                               @Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);

    void updateReportStatus(@Param("ids") List<String> ids, @Param("time") long time, @Param("userId") String userId);

    List<ReportDTO> selectByIds(@Param("ids") List<String> ids);

    void updateApiCaseStatus(@Param("ids") List<String> ids);

    List<ApiReportMessageDTO> getNoticeList(@Param("ids") List<String> ids);

    List<ExecuteReportDTO> getHistoryDeleted(@Param("ids") List<String> ids);
}
