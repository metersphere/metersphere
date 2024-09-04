package io.metersphere.plan.mapper;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plan.domain.TestPlanReportBug;
import io.metersphere.plan.dto.ReportBugCountDTO;
import io.metersphere.plan.dto.ReportBugSumDTO;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportBugMapper {

	/**
	 * 获取计划的缺陷集合
	 * @param planId 计划ID
	 * @return 缺陷集合
	 */
	List<TestPlanReportBug> getPlanBugs(@Param("id") String planId, @Param("ids") List<String> ids);

	List<String> getPlanBugsId(@Param("id") String planId);

	/**
	 * 分页查询报告关联的缺陷
	 * @param request 请求参数
	 * @return 关联的缺陷集合
	 */
	List<BugDTO> list(@Param("request") TestPlanReportDetailPageRequest request);

	/**
	 * 统计用例明细的关联缺陷数
	 * @param reportId 报告ID
	 * @return 缺陷数量
	 */
	List<ReportBugSumDTO> countBug(@Param("id") String reportId);

	/**
	 * 统计计划下的关联用例的缺陷数目
	 * @param planId 计划ID
	 * @return 缺陷数目
	 */
	List<ReportBugCountDTO> countPlanBug(@Param("id") String planId);
}
