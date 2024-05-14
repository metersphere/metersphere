package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportDeleteRequest;
import io.metersphere.plan.dto.request.TestPlanReportEditRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TestPlanReportService {

	@Resource
	private UserService userService;
	@Resource
	private TestPlanReportMapper testPlanReportMapper;
	@Resource
	private ExtTestPlanReportMapper extTestPlanReportMapper;
	@Resource
	private TestPlanReportSummaryMapper testPlanReportSummaryMapper;
	@Resource
	private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
	@Resource
	private TestPlanReportBugMapper testPlanReportBugMapper;

	/**
	 * 分页查询报告列表
	 * @param request 分页请求参数
	 * @return 报告列表
	 */
	public List<TestPlanReportPageResponse> page(TestPlanReportPageRequest request) {
		List<TestPlanReportPageResponse> reportList = extTestPlanReportMapper.list(request, request.getSortString());
		if (CollectionUtils.isEmpty(reportList)) {
			return new ArrayList<>();
		}
		List<String> distinctUserIds = reportList.stream().map(TestPlanReportPageResponse::getCreateUser).distinct().toList();
		Map<String, String> userMap = userService.getUserMapByIds(distinctUserIds);
		reportList.forEach(report -> report.setCreateUserName(userMap.get(report.getCreateUser())));
		return reportList;
	}

	/**
	 * 报告重命名
	 * @param request 请求参数
	 */
	public void rename(TestPlanReportEditRequest request) {
		checkReport(request.getId());
		TestPlanReport report = new TestPlanReport();
		report.setId(request.getId());
		report.setName(request.getName());
		testPlanReportMapper.updateByPrimaryKeySelective(report);
	}

	/**
	 * 删除单个报告
	 * @param request 请求参数
	 */
	public void delete(TestPlanReportDeleteRequest request) {
		checkReport(request.getId());
		testPlanReportMapper.deleteByPrimaryKey(request.getId());
		// 删除报告内容的关联资源表
		cleanReportAssociateResource(List.of(request.getId()));
	}

	/**
	 * 批量参数报告
	 * @param request 请求参数
	 */
	public void batchDelete(TestPlanReportBatchRequest request) {
		List<String> batchIds = getBatchIds(request);
		if (CollectionUtils.isNotEmpty(batchIds)) {
			TestPlanReportExample example = new TestPlanReportExample();
			example.createCriteria().andIdIn(batchIds);
			testPlanReportMapper.deleteByExample(example);
			// 删除报告内容的关联资源表
			cleanReportAssociateResource(batchIds);
		}
	}

	/**
	 * 清理报告关联的资源
	 * @param reportIds 报告ID集合
	 */
	public void cleanReportAssociateResource(List<String> reportIds) {
		// TODO: 删除报告关联的统计, 用例, 缺陷
		TestPlanReportSummaryExample summaryExample = new TestPlanReportSummaryExample();
		summaryExample.createCriteria().andTestPlanReportIdIn(reportIds);
		testPlanReportSummaryMapper.deleteByExample(summaryExample);
		TestPlanReportFunctionCaseExample functionCaseExample = new TestPlanReportFunctionCaseExample();
		functionCaseExample.createCriteria().andTestPlanReportIdIn(reportIds);
		testPlanReportFunctionCaseMapper.deleteByExample(functionCaseExample);
		TestPlanReportBugExample bugExample = new TestPlanReportBugExample();
		bugExample.createCriteria().andTestPlanReportIdIn(reportIds);
		testPlanReportBugMapper.deleteByExample(bugExample);
	}

	/**
	 * 通过请求参数获取批量操作的ID集合
	 * @param request 请求参数
	 * @return ID集合
	 */
	private List<String> getBatchIds(TestPlanReportBatchRequest request) {
		if (request.isSelectAll()) {
			List<String> batchIds = extTestPlanReportMapper.getReportBatchIdsByParam(request);
			if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
				batchIds.removeIf(id -> request.getExcludeIds().contains(id));
			}
			return batchIds;
		} else {
			return request.getSelectIds();
		}
	}

	/**
	 * 校验报告是否存在
	 * @param id 报告ID
	 */
	private void checkReport(String id) {
		TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(id);
		if (testPlanReport == null) {
			throw new MSException(Translator.get("test_plan_report_not_exist"));
		}
	}
}
