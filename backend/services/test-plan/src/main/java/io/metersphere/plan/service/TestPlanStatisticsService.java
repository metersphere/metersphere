package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.utils.RateCalculateUtils;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.utils.ScheduleUtils;
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
	private TestPlanMapper testPlanMapper;
	@Resource
	private TestPlanConfigMapper testPlanConfigMapper;
	@Resource
	private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
	@Resource
	private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
	@Resource
	private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
	@Resource
	private ExtTestPlanBugMapper extTestPlanBugMapper;
	@Resource
	private ScheduleMapper scheduleMapper;

	/**
	 * 计划/计划组的用例统计数据
	 *
	 * @param plans 计划集合
	 */
	public void calculateCaseCount(List<? extends TestPlanStatisticsResponse> plans) {
		/*
		 * 1. 查询计划下的用例数据集合
		 */
		List<String> planIds = plans.stream().map(TestPlanStatisticsResponse::getId).toList();
		Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = getFunctionalCaseMapByPlanIds(planIds);
		Map<String, List<TestPlanApiCase>> planApiCaseMap = getApiCaseMapByPlanIds(planIds);
		Map<String, List<TestPlanApiScenario>> planApiScenarioMap = getApiScenarioByPlanIds(planIds);
		// 计划-缺陷的关联数据
		List<TestPlanBugPageResponse> planBugs = extTestPlanBugMapper.countBugByIds(planIds);
		Map<String, List<TestPlanBugPageResponse>> planBugMap = planBugs.stream().collect(Collectors.groupingBy(TestPlanBugPageResponse::getTestPlanId));
		plans.forEach(plan -> {
			List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(plan.getId());
			plan.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
			List<TestPlanApiCase> apiCases = planApiCaseMap.get(plan.getId());
			plan.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
			List<TestPlanApiScenario> apiScenarios = planApiScenarioMap.get(plan.getId());
			plan.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
			List<TestPlanBugPageResponse> bugs = planBugMap.get(plan.getId());
			plan.setBugCount(CollectionUtils.isNotEmpty(bugs) ? bugs.size() : 0);
			plan.setCaseTotal(plan.getFunctionalCaseCount() + plan.getApiCaseCount() + plan.getApiScenarioCount());
		});
	}

	/**
	 * 计划/计划组的{通过率, 执行进度}统计数据
	 *
	 * @param planIds 计划ID集合
	 */
	public List<TestPlanStatisticsResponse> calculateRate(List<String> planIds) {
		//		查出子计划
		TestPlanExample testPlanExample = new TestPlanExample();
		testPlanExample.createCriteria().andGroupIdIn(planIds);
		List<TestPlan> childrenPlan = testPlanMapper.selectByExample(testPlanExample);
		childrenPlan.forEach(item -> planIds.add(item.getId()));

		List<TestPlanStatisticsResponse> planStatisticsResponses = new ArrayList<>();
		/*
		 * 1. 查询计划下的用例数据集合
		 * 2. 根据执行结果统计(结果小数保留两位)
		 */
		// 计划的更多配置
		TestPlanConfigExample example = new TestPlanConfigExample();
		example.createCriteria().andTestPlanIdIn(planIds);
		List<TestPlanConfig> testPlanConfigList = testPlanConfigMapper.selectByExample(example);
		Map<String, TestPlanConfig> planConfigMap = testPlanConfigList.stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, p -> p));
		// 关联的用例数据
		Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = getFunctionalCaseMapByPlanIds(planIds);
		Map<String, List<TestPlanApiCase>> planApiCaseMap = getApiCaseMapByPlanIds(planIds);
		Map<String, List<TestPlanApiScenario>> planApiScenarioMap = getApiScenarioByPlanIds(planIds);

		//查询定时任务
		ScheduleExample scheduleExample = new ScheduleExample();
		scheduleExample.createCriteria().andResourceIdIn(planIds).andResourceTypeEqualTo(ScheduleResourceType.TEST_PLAN.name());
		List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
		Map<String, Schedule> scheduleMap = schedules.stream().collect(Collectors.toMap(Schedule::getResourceId, t -> t));

		planIds.forEach(planId -> {
			TestPlanStatisticsResponse statisticsResponse = new TestPlanStatisticsResponse();
			statisticsResponse.setId(planId);

			// 测试计划组没有测试计划配置。同理，也不用参与用例等数据的计算
			if (planConfigMap.containsKey(planId)) {
				statisticsResponse.setPassThreshold(planConfigMap.get(planId).getPassThreshold());
				// 功能用例分组统计开始 (为空时, 默认为未执行)
				List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(planId);
				statisticsResponse.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
				Map<String, Long> functionalCaseResultCountMap = CollectionUtils.isEmpty(functionalCases) ? new HashMap<>(16) : functionalCases.stream().collect(
						Collectors.groupingBy(functionalCase -> Optional.ofNullable(functionalCase.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
				// 接口用例分组统计开始 (为空时, 默认为未执行)
				List<TestPlanApiCase> apiCases = planApiCaseMap.get(planId);
				statisticsResponse.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
				Map<String, Long> apiCaseResultCountMap = CollectionUtils.isEmpty(apiCases) ? new HashMap<>(16) : apiCases.stream().collect(
						Collectors.groupingBy(apiCase -> Optional.ofNullable(apiCase.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
				// 接口场景用例分组统计开始 (为空时, 默认为未执行)
				List<TestPlanApiScenario> apiScenarios = planApiScenarioMap.get(planId);
				statisticsResponse.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
				Map<String, Long> apiScenarioResultCountMap = CollectionUtils.isEmpty(apiScenarios) ? new HashMap<>(16) : apiScenarios.stream().collect(
						Collectors.groupingBy(apiScenario -> Optional.ofNullable(apiScenario.getLastExecResult()).orElse(ExecStatus.PENDING.name()), Collectors.counting()));
				// 用例数据汇总
				statisticsResponse.setSuccessCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.SUCCESS.name()));
				statisticsResponse.setErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.ERROR.name()));
				statisticsResponse.setFakeErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.FAKE_ERROR.name()));
				statisticsResponse.setBlockCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.BLOCKED.name()));
				statisticsResponse.setPendingCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.PENDING.name()));
				statisticsResponse.setCaseTotal(statisticsResponse.getFunctionalCaseCount() + statisticsResponse.getApiCaseCount() + statisticsResponse.getApiScenarioCount());
				// 通过率 {通过用例数/总用例数} && 执行进度 {非未执行的用例数/总用例数}
				statisticsResponse.setPassRate(RateCalculateUtils.divWithPrecision(statisticsResponse.getSuccessCount(), statisticsResponse.getCaseTotal(), 2));
				statisticsResponse.setExecuteRate(RateCalculateUtils.divWithPrecision(statisticsResponse.getCaseTotal() - statisticsResponse.getPendingCount(), statisticsResponse.getCaseTotal(), 2));
			}
			planStatisticsResponses.add(statisticsResponse);

			//定时任务
			if (scheduleMap.containsKey(planId)) {
				Schedule schedule = scheduleMap.get(planId);
				BaseScheduleConfigRequest request = new BaseScheduleConfigRequest();
				request.setEnable(schedule.getEnable());
				request.setCron(schedule.getValue());
				request.setResourceId(planId);
				if (schedule.getConfig() != null) {
					request.setRunConfig(JSON.parseObject(schedule.getConfig(), Map.class));
				}
				statisticsResponse.setScheduleConfig(request);
				if (schedule.getEnable()) {
					statisticsResponse.setNextTriggerTime(ScheduleUtils.getNextTriggerTime(schedule.getValue()));
				}
			}

		});
		return planStatisticsResponses;
	}


	private Map<String, List<TestPlanFunctionalCase>> getFunctionalCaseMapByPlanIds(List<String> planIds) {
		// 计划或者组-功能用例的关联数据
		List<TestPlanFunctionalCase> planFunctionalCases = extTestPlanFunctionalCaseMapper.getPlanFunctionalCaseByIds(planIds);
		return planFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
	}

	private Map<String, List<TestPlanApiCase>> getApiCaseMapByPlanIds(List<String> planIds) {
		// 计划或者组-接口用例的关联数据
		List<TestPlanApiCase> planApiCases = extTestPlanApiCaseMapper.getPlanApiCaseByIds(planIds);
		return planApiCases.stream().collect(Collectors.groupingBy(TestPlanApiCase::getTestPlanId));
	}

	private Map<String, List<TestPlanApiScenario>> getApiScenarioByPlanIds(List<String> planIds) {
		// 计划或者组-场景用例的关联数据
		List<TestPlanApiScenario> planApiScenarios = extTestPlanApiScenarioMapper.getPlanApiScenarioByIds(planIds);
		return planApiScenarios.stream().collect(Collectors.groupingBy(TestPlanApiScenario::getTestPlanId));
	}

	/**
	 * 汇总计划下所有的用例的集合
	 * @param functionalCaseMap 功能用例
	 * @param apiCaseMap 接口用例
	 * @param apiScenarioMap 接口场景
	 * @param countKey 汇总的key
	 * @return 总数
	 */
	private Integer countCaseMap(Map<String, Long> functionalCaseMap, Map<String, Long> apiCaseMap, Map<String, Long> apiScenarioMap, String countKey) {
		return (functionalCaseMap.containsKey(countKey) ? functionalCaseMap.get(countKey).intValue() : 0) +
				(apiCaseMap.containsKey(countKey) ? apiCaseMap.get(countKey).intValue() : 0) +
				(apiScenarioMap.containsKey(countKey) ? apiScenarioMap.get(countKey).intValue() : 0);
	}
}
