package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
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

    List<TestPlanReport> selectReportByIds(@Param("ids") List<String> ids);

    List<ApiReportMessageDTO> getNoticeList(@Param("ids") List<String> subList);

	long countReportByTime(@Param("time") long timeMills, @Param("projectId") String projectId);

	List<String> selectReportIdByProjectIdAndTime(@Param("time") long timeMills, @Param("projectId") String projectId);

	List<String> selectReportIdTestPlanIds(@Param("testPlanIds") List<String> testPlanIds);
}
