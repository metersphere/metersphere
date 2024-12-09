package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.utils.ScheduleUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
	@Resource
	private TestPlanBaseUtilsService testPlanBaseUtilsService;

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

	private Map<String, TestPlanConfig> selectConfig(List<String> testPlanIds) {
		TestPlanConfigExample example = new TestPlanConfigExample();
		example.createCriteria().andTestPlanIdIn(testPlanIds);
		List<TestPlanConfig> testPlanConfigList = testPlanConfigMapper.selectByExample(example);
		return testPlanConfigList.stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, p -> p));
	}

	private Map<String, Schedule> selectSchedule(List<String> testPlanIds) {
		ScheduleExample scheduleExample = new ScheduleExample();
		scheduleExample.createCriteria().andResourceIdIn(testPlanIds).andResourceTypeIn(List.of(ScheduleResourceType.TEST_PLAN.name(), ScheduleResourceType.TEST_PLAN_GROUP.name()));
		List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
		return schedules.stream().collect(Collectors.toMap(Schedule::getResourceId, t -> t));
	}

	/**
	 * 计划/计划组的{通过率, 执行进度, 状态}统计数据
	 *
	 */
	public List<TestPlanStatisticsResponse> calculateRate(List<String> paramIds) {
		//		查出所有计划
		TestPlanExample testPlanExample = new TestPlanExample();
		testPlanExample.createCriteria().andIdIn(paramIds);
		List<TestPlan> paramTestPlanList = testPlanMapper.selectByExample(testPlanExample);
		testPlanExample.clear();
		testPlanExample.createCriteria().andGroupIdIn(paramIds);
		List<TestPlan> childrenTestPlan = testPlanMapper.selectByExample(testPlanExample);
		paramTestPlanList.removeAll(childrenTestPlan);
		List<String> allTestPlanIdList = new ArrayList<>();
		allTestPlanIdList.addAll(paramTestPlanList.stream().map(TestPlan::getId).toList());
		allTestPlanIdList.addAll(childrenTestPlan.stream().map(TestPlan::getId).toList());

		Map<TestPlan, List<TestPlan>> groupTestPlanMap = new HashMap<>();
		for (TestPlan testPlan : paramTestPlanList) {
			List<TestPlan> children = new ArrayList<>();
			for (TestPlan child : childrenTestPlan) {
				if (StringUtils.equalsIgnoreCase(child.getGroupId(), testPlan.getId())) {
					children.add(child);
				}
			}
			groupTestPlanMap.put(testPlan, children);
			childrenTestPlan.removeAll(children);
		}
		childrenTestPlan = null;
		paramTestPlanList = null;

		List<TestPlanStatisticsResponse> returnResponse = new ArrayList<>();

		// 计划的更多配置
		Map<String, TestPlanConfig> planConfigMap = this.selectConfig(allTestPlanIdList);
		// 关联的用例数据
		Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap = getFunctionalCaseMapByPlanIds(allTestPlanIdList);
		Map<String, List<TestPlanApiCase>> planApiCaseMap = getApiCaseMapByPlanIds(allTestPlanIdList);
		Map<String, List<TestPlanApiScenario>> planApiScenarioMap = getApiScenarioByPlanIds(allTestPlanIdList);
		//查询定时任务
		Map<String, Schedule> scheduleMap = this.selectSchedule(allTestPlanIdList);

		groupTestPlanMap.forEach((rootPlan, children) -> {
			TestPlanStatisticsResponse rootResponse = this.genTestPlanStatisticsResponse(rootPlan, planConfigMap, planFunctionalCaseMap, planApiCaseMap, planApiScenarioMap, scheduleMap);
			rootResponse.setStatus(rootPlan.getStatus());

			List<TestPlanStatisticsResponse> childrenResponse = new ArrayList<>();
			if (!CollectionUtils.isEmpty(children)) {
				List<String> childStatus = new ArrayList<>();
				AtomicBoolean hasNotPassedChild = new AtomicBoolean(false);
				children.forEach(child -> {
					TestPlanStatisticsResponse childResponse = this.genTestPlanStatisticsResponse(child, planConfigMap, planFunctionalCaseMap, planApiCaseMap, planApiScenarioMap, scheduleMap);
					if (BooleanUtils.isFalse(childResponse.isPass())) {
						hasNotPassedChild.set(true);
					}
					childResponse.setStatus(child.getStatus());
					
					childResponse.calculateStatus();
					childStatus.add(childResponse.getStatus());
					//添加到rootResponse中
					rootResponse.calculateAllNumber(childResponse);
					childrenResponse.add(childResponse);
				});
				rootResponse.calculateCaseTotal();
				rootResponse.calculatePassRate();
				rootResponse.calculateExecuteRate();
				if (!StringUtils.equalsIgnoreCase(rootResponse.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
					rootResponse.setStatus(testPlanBaseUtilsService.calculateStatusByChildren(childStatus));
				}
				if (!hasNotPassedChild.get()) {
					rootResponse.setPass(true);
				}
			} else {
				rootResponse.calculateCaseTotal();
				rootResponse.calculatePassRate();
				rootResponse.calculateExecuteRate();
				rootResponse.calculateStatus();
				rootResponse.calculateTestPlanIsPass();
			}
			returnResponse.add(rootResponse);
			returnResponse.addAll(childrenResponse);
		});
		return returnResponse;
	}

	public Map<String, Long> countApiScenarioExecResultMap(List<TestPlanApiScenario> apiScenarios) {
		return CollectionUtils.isEmpty(apiScenarios) ? new HashMap<>(16) : apiScenarios.stream().collect(
				Collectors.groupingBy(apiScenario -> StringUtils.isEmpty(apiScenario.getLastExecResult()) ? ExecStatus.PENDING.name() : apiScenario.getLastExecResult(), Collectors.counting()));
	}

	public Map<String, Long> countApiTestCaseExecResultMap(List<TestPlanApiCase> apiCases) {
		return CollectionUtils.isEmpty(apiCases) ? new HashMap<>(16) : apiCases.stream().collect(
				Collectors.groupingBy(apiCase -> StringUtils.isEmpty(apiCase.getLastExecResult()) ? ExecStatus.PENDING.name() : apiCase.getLastExecResult(), Collectors.counting()));
	}

	public Map<String, Long> countFunctionalCaseExecResultMap(List<TestPlanFunctionalCase> functionalCases) {
		return CollectionUtils.isEmpty(functionalCases) ? new HashMap<>(16) : functionalCases.stream().collect(
				Collectors.groupingBy(functionalCase -> StringUtils.isEmpty(functionalCase.getLastExecResult()) ? ExecStatus.PENDING.name() : functionalCase.getLastExecResult(), Collectors.counting()));
	}

	private TestPlanStatisticsResponse genTestPlanStatisticsResponse(TestPlan child,
																	 Map<String, TestPlanConfig> planConfigMap,
																	 Map<String, List<TestPlanFunctionalCase>> planFunctionalCaseMap,
																	 Map<String, List<TestPlanApiCase>> planApiCaseMap,
																	 Map<String, List<TestPlanApiScenario>> planApiScenarioMap,
																	 Map<String, Schedule> scheduleMap) {
		String planId = child.getId();
		TestPlanStatisticsResponse statisticsResponse = new TestPlanStatisticsResponse();
		statisticsResponse.setId(planId);
		// 测试计划组没有测试计划配置。同理，也不用参与用例等数据的计算
		if (planConfigMap.containsKey(planId)) {
			statisticsResponse.setPassThreshold(planConfigMap.get(planId).getPassThreshold());

			List<TestPlanFunctionalCase> functionalCases = planFunctionalCaseMap.get(planId);
			List<TestPlanApiCase> apiCases = planApiCaseMap.get(planId);
			List<TestPlanApiScenario> apiScenarios = planApiScenarioMap.get(planId);


			// 功能用例分组统计开始 (为空时, 默认为未执行)
			Map<String, Long> functionalCaseResultCountMap = this.countFunctionalCaseExecResultMap(functionalCases);
			// 接口用例分组统计开始 (为空时, 默认为未执行)
			Map<String, Long> apiCaseResultCountMap = this.countApiTestCaseExecResultMap(apiCases);
			// 接口场景用例分组统计开始 (为空时, 默认为未执行)
			Map<String, Long> apiScenarioResultCountMap = this.countApiScenarioExecResultMap(apiScenarios);

			// 用例数据汇总
			statisticsResponse.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
			statisticsResponse.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
			statisticsResponse.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
			statisticsResponse.setSuccessCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.SUCCESS.name()));
			statisticsResponse.setErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.ERROR.name()));
			statisticsResponse.setFakeErrorCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.FAKE_ERROR.name()));
			statisticsResponse.setBlockCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.BLOCKED.name()));
			statisticsResponse.setPendingCount(countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.PENDING.name()));
			statisticsResponse.calculateCaseTotal();
			statisticsResponse.calculatePassRate();
			statisticsResponse.calculateExecuteRate();
			statisticsResponse.calculateTestPlanIsPass();
		}
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
		return statisticsResponse;
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
	public Integer countCaseMap(Map<String, Long> functionalCaseMap, Map<String, Long> apiCaseMap, Map<String, Long> apiScenarioMap, String countKey) {
		return (functionalCaseMap.containsKey(countKey) ? functionalCaseMap.get(countKey).intValue() : 0) +
				(apiCaseMap.containsKey(countKey) ? apiCaseMap.get(countKey).intValue() : 0) +
				(apiScenarioMap.containsKey(countKey) ? apiScenarioMap.get(countKey).intValue() : 0);
	}
}
