package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenarioBlob;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportStepDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioReportMapper {
    List<ApiScenarioReport> list(@Param("request") ApiReportPageRequest request);

    List<String> getIds(@Param("request") ApiReportBatchRequest request);

    List<ApiScenarioReport> selectApiReportByIds(@Param("ids") List<String> ids);

    List<ApiScenarioReportStepDTO> selectStepByReportId(@Param("reportId") String reportId);

    List<String> selectApiScenarioReportByProjectId(String projectId);

    int selectReportByTime(@Param("time") long time, @Param("projectId") String projectId);

    int selectScenarioReportByTime(@Param("time") long time, @Param("projectId") String projectId);

    List<String> selectApiReportByProjectIdAndTime(@Param("time") long time, @Param("projectId") String projectId);

    List<TaskCenterDTO> taskCenterlist(@Param("request") TaskCenterPageRequest request, @Param("projectIds") List<String> projectIds,
                                       @Param("startTime") long startTime, @Param("endTime") long endTime);


    List<ReportDTO> getReports(@Param("request") TaskCenterBatchRequest request, @Param("projectIds") List<String> projectIds,
                               @Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);

    void updateReportStatus(@Param("ids") List<String> ids, @Param("time") long time, @Param("userId") String userId);

    List<ReportDTO> selectByIds(@Param("ids") List<String> ids);

    /**
     * 根据项目获取组织id
     */
    List<ReportDTO> getOrgListByProjectIds(@Param("ids") List<String> ids);

    ApiScenarioBlob getScenarioBlob(String id);

    void updateApiScenario(List<String> subList);

    List<ApiScenarioReportStepDTO> selectStepDeatilByReportId(String id);
}
