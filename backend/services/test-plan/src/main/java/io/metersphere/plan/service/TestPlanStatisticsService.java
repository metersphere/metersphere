package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanConfigExample;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.utils.RateCalculateUtils;
import io.metersphere.sdk.constants.FunctionalCaseExecuteResult;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试计划统计相关业务
 */
@Service
public class TestPlanStatisticsService {

	@Resource
	private TestPlanConfigMapper testPlanConfigMapper;
	@Resource
	private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;

	/**
	 * 计划的用例统计数据
	 *
	 * @param plans 计划集合
	 */
	public void calculateCaseCount(List<? extends TestPlanStatisticsResponse> plans) {
		// TODO 计算计划下各种维度的用例统计数目 (待定)
		/*
		 * 1. 查询计划下的用例数据集合(目前只有功能用例)
		 * 2. 根据执行结果统计(结果小数保留两位)
		 */

		List<String> planIds = plans.stream().map(TestPlanStatisticsResponse::getId).toList();
		// 计划-功能用例的关联数据
		List<TestPlanFunctionalCase> planFunctionalCases = extTestPlanFunctionalCaseMapper.getPlanFunctionalCaseByIds(planIds);
		Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = planFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
		// TODO: 计划-接口用例的关联数据
		plans.forEach(plan -> {
			// 功能用例统计开始
			List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(plan.getId());
			plan.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
			// TODO: 接口用例统计开始

			// FIXME: CaseTotal后续会补充接口用例及场景的统计数据
			plan.setCaseTotal(plan.getFunctionalCaseCount());
		});
	}

	/**
	 * 计划的{通过率, 执行进度}统计数据
	 *
	 * @param planIds 计划ID集合
	 */
	public List<TestPlanStatisticsResponse> calculateRate(List<String> planIds) {
		List<TestPlanStatisticsResponse> planStatisticsResponses = new ArrayList<>();
		// TODO 计算计划下的用例通过率, 执行进度 (待定)
		/*
		 * 1. 查询计划下的用例数据集合(目前只有功能用例)
		 * 2. 根据执行结果统计(结果小数保留两位)
		 */

		// 计划的更多配置
		TestPlanConfigExample example = new TestPlanConfigExample();
		example.createCriteria().andTestPlanIdIn(planIds);
		List<TestPlanConfig> testPlanConfigList = testPlanConfigMapper.selectByExample(example);
		Map<String, TestPlanConfig> planConfigMap = testPlanConfigList.stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, p -> p));
		// 计划-功能用例的关联数据
		List<TestPlanFunctionalCase> planFunctionalCases = extTestPlanFunctionalCaseMapper.getPlanFunctionalCaseByIds(planIds);
		Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = planFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
		// TODO: 计划-接口用例的关联数据
		planIds.forEach(planId -> {
			TestPlanStatisticsResponse statisticsResponse = new TestPlanStatisticsResponse();
			statisticsResponse.setId(planId);
			statisticsResponse.setPassThreshold(planConfigMap.get(planId).getPassThreshold());
			int success = 0, error = 0, fakeError = 0, block = 0, pending = 0;
			// 功能用例统计开始
			List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(planId);
			statisticsResponse.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
			// 根据执行结果分组(为空, 默认为未执行)
			Map<String, List<TestPlanFunctionalCase>> functionalCaseResultMap = CollectionUtils.isEmpty(functionalCases) ? new HashMap<>(16) : functionalCases.stream().collect(
					Collectors.groupingBy(functionalCase -> Optional.ofNullable(functionalCase.getLastExecResult()).orElse(FunctionalCaseExecuteResult.PENDING.name())));
			success += functionalCaseResultMap.containsKey(FunctionalCaseExecuteResult.SUCCESS.name()) ? functionalCaseResultMap.get(FunctionalCaseExecuteResult.SUCCESS.name()).size() : 0;
			error += functionalCaseResultMap.containsKey(FunctionalCaseExecuteResult.ERROR.name()) ? functionalCaseResultMap.get(FunctionalCaseExecuteResult.ERROR.name()).size() : 0;
			fakeError += functionalCaseResultMap.containsKey(FunctionalCaseExecuteResult.FAKE_ERROR.name()) ? functionalCaseResultMap.get(FunctionalCaseExecuteResult.FAKE_ERROR.name()).size() : 0;
			block += functionalCaseResultMap.containsKey(FunctionalCaseExecuteResult.BLOCKED.name()) ? functionalCaseResultMap.get(FunctionalCaseExecuteResult.BLOCKED.name()).size() : 0;
			pending += functionalCaseResultMap.containsKey(FunctionalCaseExecuteResult.PENDING.name()) ? functionalCaseResultMap.get(FunctionalCaseExecuteResult.PENDING.name()).size() : 0;
			// TODO: 接口用例统计开始


			// 用例数据汇总
			statisticsResponse.setSuccessCount(success);
			statisticsResponse.setErrorCount(error);
			statisticsResponse.setFakeErrorCount(fakeError);
			statisticsResponse.setBlockCount(block);
			statisticsResponse.setPendingCount(pending);
			// FIXME: CaseTotal后续会补充接口用例及场景的统计数据
			statisticsResponse.setCaseTotal(statisticsResponse.getFunctionalCaseCount());
			// 通过率 {通过用例数/总用例数} && 执行进度 {非未执行的用例数/总用例数}
			statisticsResponse.setPassRate(RateCalculateUtils.divWithPrecision(statisticsResponse.getSuccessCount(), statisticsResponse.getCaseTotal(), 2));
			statisticsResponse.setExecuteRate(RateCalculateUtils.divWithPrecision(statisticsResponse.getCaseTotal() - statisticsResponse.getPendingCount(), statisticsResponse.getCaseTotal(), 2));
			planStatisticsResponses.add(statisticsResponse);
		});
		return planStatisticsResponses;
	}
}
