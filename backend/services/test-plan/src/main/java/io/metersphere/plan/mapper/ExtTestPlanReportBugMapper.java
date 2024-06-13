package io.metersphere.plan.mapper;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plan.domain.TestPlanReportBug;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportBugMapper {

	/**
	 * 获取计划的缺陷集合
	 * @param planId 计划ID
	 * @return 缺陷集合
	 */
	List<TestPlanReportBug> getPlanBugs(@Param("id") String planId);

	/**
	 * 获取计划的缺陷集合
	 * @param planIds 计划ID集合
	 * @return 缺陷集合
	 */
	List<TestPlanReportBug> getGroupBugs(@Param("ids") List<String> planIds);

	/**
	 * 分页查询报告关联的缺陷
	 * @param request 请求参数
	 * @return 关联的缺陷集合
	 */
	List<BugDTO> list(@Param("request") TestPlanReportDetailPageRequest request);
}
