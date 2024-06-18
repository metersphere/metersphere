package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportFunctionCase;
import io.metersphere.plan.dto.CaseStatusCountMap;
import io.metersphere.plan.dto.ReportDetailCasePageDTO;
import io.metersphere.plan.dto.TestPlanBaseModule;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import io.metersphere.plugin.platform.dto.SelectOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportFunctionalCaseMapper {

	/**
	 * 获取计划关联的功能用例
	 * @param planId 计划ID
	 * @return 功能用例列表
	 */
	List<TestPlanReportFunctionCase> getPlanExecuteCases(@Param("id") String planId);

	/**
	 * 获取项目下功能用例所属模块集合
	 * @param projectId 计划ID
	 * @return 模块集合
	 */
	List<TestPlanBaseModule> getPlanExecuteCaseModules(@Param("id") String projectId);

	/**
	 * 获取用例等级
	 * @param caseIds 用例集合
	 * @return 等级集合
	 */
	List<SelectOption> getCasePriorityByIds(@Param("ids") List<String> caseIds);

	/**
	 * 统计报告中执行通过的功能用例数量
	 * @param reportId 报告ID
	 * @return 用例数量
	 */
	Long countExecuteSuccessCase(@Param("id") String reportId);

	/**
	 * 统计报告中功能用例执行情况
	 * @param reportId 报告ID
	 * @return 用例数量
	 */
	List<CaseStatusCountMap> countExecuteResult(@Param("id") String reportId);

	/**
	 * 分页查询报告关联的用例
	 * @param request 请求参数
	 * @return 关联的用例集合
	 */
	List<ReportDetailCasePageDTO> list(@Param("request") TestPlanReportDetailPageRequest request);

	/**
	 * 统计用例明细的关联缺陷数
	 * @param reportId 报告ID
	 * @return 缺陷数量
	 */
	Long countBug(@Param("id") String reportId);
}
