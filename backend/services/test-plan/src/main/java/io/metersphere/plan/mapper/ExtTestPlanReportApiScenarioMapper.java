package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.dto.CaseStatusCountMap;
import io.metersphere.plan.dto.ReportDetailCasePageDTO;
import io.metersphere.plan.dto.TestPlanBaseModule;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanReportApiScenarioMapper {

	/**
	 * 统计报告中场景用例执行情况
	 * @param reportId 报告ID
	 * @return 用例数量
	 */
	List<CaseStatusCountMap> countExecuteResult(@Param("id") String reportId);

	/**
	 * 获取计划关联的场景用例
	 * @param planId 计划ID
	 * @return 场景用例列表
	 */
	List<TestPlanReportApiScenario> getPlanExecuteCases(@Param("id") String planId);

	/**
	 * 获取项目下场景用例所属模块集合
	 * @param projectId 计划ID
	 * @return 模块集合
	 */
	List<TestPlanBaseModule> getPlanExecuteCaseModules(@Param("id") String projectId);

	/**
	 * 分页查询报告关联的用例
	 * @param request 请求参数
	 * @return 关联的用例集合
	 */
	List<ReportDetailCasePageDTO> list(@Param("request") TestPlanReportDetailPageRequest request);
}
