package io.metersphere.plan.mapper;

import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportDetailResponse;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.system.dto.sdk.ApiReportMessageDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportMapper {

    /**
     * 分页获取计划列表
     *
     * @param request 分页请求参数
     * @return 计划列表
     */
    @BaseConditionFilter
    List<TestPlanReportPageResponse> list(@Param("request") TestPlanReportPageRequest request);

    /**
     * 根据页面参数获取批量操作的报告ID
     *
     * @param request 请求参数
     * @return 报告ID集合
     */
    @BaseConditionFilter
    List<String> getReportBatchIdsByParam(@Param("request") TestPlanReportBatchRequest request);

    List<TestPlanReport> selectReportByIds(@Param("ids") List<String> ids);

    List<ApiReportMessageDTO> getNoticeList(@Param("ids") List<String> subList);

    long countReportByTime(@Param("time") long timeMills, @Param("projectId") String projectId);

    List<String> selectReportIdByProjectIdAndTime(@Param("time") long timeMills, @Param("projectId") String projectId);

    List<String> selectReportIdTestPlanIds(@Param("testPlanIds") List<String> testPlanIds);

    List<TaskCenterDTO> taskCenterlist(@Param("request") TaskCenterPageRequest request, @Param("projectIds") List<String> projectIds,
                                       @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<TaskCenterDTO> getChildTaskCenter(@Param("ids") List<String> groupReportIds);

    @BaseConditionFilter
    List<TestPlanReportDetailResponse> getPlanReportListById(@Param("request") TestPlanReportDetailPageRequest request);

    List<ReportDTO> getReports(@Param("request") TaskCenterBatchRequest request, @Param("projectIds") List<String> projectIds,
                               @Param("ids") List<String> ids, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<ExecuteReportDTO> getHistoryDeleted(@Param("ids") List<String> ids);

    List<ReportDTO> selectByParentIds(@Param("ids") List<String> ids);

    List<ReportDTO> getCaseReports(@Param("ids") List<String> ids);

    List<ReportDTO> getScenarioReports(@Param("ids") List<String> ids);

    void batchUpdateExecuteTimeAndStatus(@Param("startTime") long startTime, @Param("ids") List<String> ids);

    List<ReportDTO> getReportsByIds(@Param("ids") List<String> ids);

    void deleteGroupReport(@Param("id") String id);

    List<TestPlanReport> getChildrenReport(@Param("reportId") String reportId);

    void resetRerunReport(@Param("reportId") String reportId);

    List<TestPlanReport> getPlanChildrenTask(@Param("reportId") String reportId);
}
