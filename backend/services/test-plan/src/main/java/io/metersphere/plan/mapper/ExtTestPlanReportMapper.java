package io.metersphere.plan.mapper;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.dto.CaseStatusCountMap;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.system.dto.sdk.ApiReportMessageDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportMapper {

    /**
     * 分页获取计划列表
     *
     * @param request 分页请求参数
     * @return 计划列表
     */
    List<TestPlanReportPageResponse> list(@Param("request") TestPlanReportPageRequest request);

    /**
     * 根据页面参数获取批量操作的报告ID
     *
     * @param request 请求参数
     * @return 报告ID集合
     */
    List<String> getReportBatchIdsByParam(@Param("request") TestPlanReportBatchRequest request);

    /**
     * 统计报告中执行通过的功能用例数量
     * @param reportId 报告ID
     * @return 用例数量
     */
    Long countExecuteSuccessFunctionalCase(@Param("id") String reportId);

    List<TestPlanReport> selectReportByIds(@Param("ids") List<String> ids);

    List<ApiReportMessageDTO> getNoticeList(@Param("ids") List<String> subList);

    /**
     * 统计报告中功能用例执行情况
     * @param reportId 报告ID
     * @return 用例数量
     */
    List<CaseStatusCountMap> countFunctionalCaseExecuteResult(@Param("id") String reportId);

    /**
     * 分页查询报告关联的缺陷
     * @param request 请求参数
     * @return 关联的缺陷集合
     */
    List<BugDTO> listReportBugs(@Param("request")TestPlanReportDetailPageRequest request);

    /**
     * 分页查询报告关联的用例
     * @param request 请求参数
     * @return 关联的用例集合
     */
    List<FunctionalCasePageDTO> listReportFunctionalCases(@Param("request")TestPlanReportDetailPageRequest request);

    long countReportByTime(@Param("time") long timeMills, @Param("projectId") String projectId);

    List<String> selectReportIdByProjectIdAndTime(@Param("time") long timeMills, @Param("projectId") String projectId);

    List<String> selectReportIdTestPlanIds(@Param("testPlanIds") List<String> testPlanIds);
}
