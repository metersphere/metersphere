package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.dto.TestPlanShareInfo;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanReportShareRequest;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.enums.TestPlanStatus;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.TestPlanMessageDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanSendNoticeService {

	@Resource
	private FunctionalCaseMapper functionalCaseMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private TestPlanMapper testPlanMapper;
	@Resource
	private NoticeSendService noticeSendService;
	@Resource
	private CommonNoticeSendService commonNoticeSendService;
	@Resource
	private TestPlanConfigMapper testPlanConfigMapper;
	@Resource
	private TestPlanFollowerMapper testPlanFollowerMapper;
	@Autowired
	private ProjectMapper projectMapper;
	@Resource
	private TestPlanReportShareService testPlanReportShareService;
	@Resource
	private TestPlanStatisticsService testPlanStatisticsService;

	public void sendNoticeCase(List<String> relatedUsers, String userId, String caseId, String task, String event, String testPlanId) {
		FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(caseId);
		User user = userMapper.selectByPrimaryKey(userId);
		setLanguage(user.getLanguage());
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
		Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
		String template = defaultTemplateMap.get(task + "_" + event);
		Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
		String subject = defaultSubjectMap.get(task + "_" + event);
		Map paramMap;
		BeanMap beanMap = new BeanMap(functionalCase);
		paramMap = new HashMap<>(beanMap);
		paramMap.put("testPlanName", testPlan.getName());
		paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
		NoticeModel noticeModel = NoticeModel.builder()
				.operator(user.getId())
				.context(template)
				.subject(subject)
				.paramMap(paramMap)
				.event(event)
				.status((String) paramMap.get("status"))
				.excludeSelf(true)
				.relatedUsers(relatedUsers)
				.build();
		noticeSendService.send(task, noticeModel);
	}

	private static void setLanguage(String language) {
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (StringUtils.containsIgnoreCase(language, "US")) {
			locale = Locale.US;
		} else if (StringUtils.containsIgnoreCase(language, "TW")) {
			locale = Locale.TAIWAN;
		}
		LocaleContextHolder.setLocale(locale);
	}


	public void batchSendNotice(String projectId, List<String> ids, User user, String event) {
		int amount = 100;// 每次读取的条数
		long roundTimes = Double.valueOf(Math.ceil((double) ids.size() / amount)).longValue();// 循环的次数
		for (int i = 0; i < (int) roundTimes; i++) {
			int fromIndex = (i * amount);
			int toIndex = ((i + 1) * amount);
			if (i == roundTimes - 1 || toIndex > ids.size()) {// 最后一次遍历
				toIndex = ids.size();
			}
			List<String> subList = ids.subList(fromIndex, toIndex);
			List<TestPlanDTO> testPlanDTOS = handleBatchNotice(projectId, subList);
			List<Map> resources = new ArrayList<>();
			resources.addAll(JSON.parseArray(JSON.toJSONString(testPlanDTOS), Map.class));
			commonNoticeSendService.sendNotice(NoticeConstants.TaskType.TEST_PLAN_TASK, event, resources, user, projectId);
		}
	}

	private List<TestPlanDTO> handleBatchNotice(String projectId, List<String> ids) {
		List<TestPlanDTO> dtoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(ids)) {
			Map<String, TestPlan> testPlanMap = getTestPlanInfo(projectId, ids);
			ids.forEach(id -> {
				if (testPlanMap.containsKey(id)) {
					TestPlan testPlan = testPlanMap.get(id);
					TestPlanDTO testPlanDTO = new TestPlanDTO();
					BeanUtils.copyBean(testPlanDTO, testPlan);
					dtoList.add(testPlanDTO);
				}
			});
		}
		return dtoList;
	}

	private Map<String, TestPlan> getTestPlanInfo(String projectId, List<String> ids) {
		TestPlanExample example = new TestPlanExample();
		example.createCriteria().andProjectIdEqualTo(projectId).andIdIn(ids);
		List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
		return testPlans.stream().collect(Collectors.toMap(TestPlan::getId, testPlan -> testPlan));
	}

	@SuppressWarnings("unused")
	public TestPlanDTO sendAddNotice(TestPlanCreateRequest request) {
		TestPlanDTO dto = new TestPlanDTO();
		BeanUtils.copyBean(dto, request);
		dto.setStatus(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED);
		return dto;
	}

	@SuppressWarnings("unused")
	public TestPlanDTO sendUpdateNotice(TestPlanUpdateRequest request) {
		TestPlanDTO dto = new TestPlanDTO();
		BeanUtils.copyBean(dto, request);
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getId());
		dto.setStatus(testPlan.getStatus());
		dto.setType(testPlan.getType());
		dto.setActualEndTime(testPlan.getActualEndTime());
		dto.setActualStartTime(testPlan.getActualStartTime());
		dto.setCreateTime(testPlan.getCreateTime());
		dto.setCreateUser(testPlan.getCreateUser());
		dto.setUpdateTime(testPlan.getUpdateTime());
		dto.setUpdateUser(testPlan.getUpdateUser());
		dto.setNum(testPlan.getNum());
		return dto;
	}

	@SuppressWarnings("unused")
	public TestPlanDTO sendDeleteNotice(String id) {
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
		TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(id);
		TestPlanFollowerExample example = new TestPlanFollowerExample();
		example.createCriteria().andTestPlanIdEqualTo(id);
		List<TestPlanFollower> testPlanFollowers = testPlanFollowerMapper.selectByExample(example);
		List<String> followUsers = testPlanFollowers.stream().map(TestPlanFollower::getUserId).toList();
		TestPlanDTO dto = new TestPlanDTO();
		if (testPlan != null) {
			BeanUtils.copyBean(dto, testPlan);
			BeanUtils.copyBean(dto, testPlanConfig);
			dto.setFollowUsers(followUsers);
			return dto;
		}
		return null;
	}

	/**
	 * 报告汇总-计划执行结束通知
	 *
	 * @param currentUser 当前用户
	 * @param planId      计划ID
	 * @param report      报告
	 */
	@Async
	public void sendExecuteNotice(String currentUser, String planId, TestPlanReport report) {
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
		if (testPlan != null) {
			User user = userMapper.selectByPrimaryKey(currentUser);
			setLanguage(user.getLanguage());
			Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
			String template = defaultTemplateMap.get(StringUtils.equals(report.getResultStatus(), ResultStatus.SUCCESS.name()) ?
					NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_SUCCESSFUL : NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_FAILED);
			Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
			String subject = defaultSubjectMap.get(StringUtils.equals(report.getResultStatus(), ResultStatus.SUCCESS.name()) ?
					NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_SUCCESSFUL : NoticeConstants.TemplateText.TEST_PLAN_TASK_EXECUTE_FAILED);
			TestPlanMessageDTO messageDTO = buildMessageNotice(planId, report, currentUser);
			BeanMap beanMap = new BeanMap(messageDTO);
			Map paramMap = new HashMap<>(beanMap);
			paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
			paramMap.put("name", testPlan.getName());
			paramMap.put("projectId", report.getProjectId());
			paramMap.put("id", planId);
			paramMap.put("Language", user.getLanguage());
			paramMap.put("createUser", testPlan.getCreateUser());
			NoticeModel noticeModel = NoticeModel.builder().operator(currentUser).excludeSelf(false)
					.context(template).subject(subject).paramMap(paramMap).event(StringUtils.equals(report.getResultStatus(), ResultStatus.SUCCESS.name()) ?
							NoticeConstants.Event.EXECUTE_SUCCESSFUL : NoticeConstants.Event.EXECUTE_FAILED).build();
			noticeSendService.send(StringUtils.equals(TaskTriggerMode.API.name(), report.getTriggerMode()) ?
					NoticeConstants.TaskType.JENKINS_TASK : NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
		}
	}

	/**
	 * 构建计划消息通知对象
	 *
	 * @param planId      计划ID
	 * @param report      报告
	 * @param currentUser 当前用户
	 * @return 计划消息通知对象
	 */
	private TestPlanMessageDTO buildMessageNotice(String planId, TestPlanReport report, String currentUser) {
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
		TestPlanStatisticsResponse statistics = testPlanStatisticsService.calculateRate(List.of(planId)).getFirst();
		// 报告URL
		Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
		SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
		assert systemParameterService != null;
		BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
		String reportUrl = baseSystemConfigDTO.getUrl() + "/#/test-plan/testPlanReportDetail?id=%s&type=%s&orgId=%s&pId=%s";
		reportUrl = String.format(reportUrl, report.getId(), report.getIntegrated() ? TestPlanConstants.TEST_PLAN_TYPE_GROUP : TestPlanConstants.TEST_PLAN_TYPE_PLAN,
				project.getOrganizationId(), project.getId());
		// 报告分享URL
		TestPlanReportShareRequest shareRequest = new TestPlanReportShareRequest();
		shareRequest.setReportId(report.getId());
		shareRequest.setProjectId(project.getId());
		shareRequest.setShareType("TEST_PLAN_SHARE_REPORT");
		TestPlanShareInfo shareInfo = testPlanReportShareService.gen(shareRequest, currentUser);
		String reportShareUrl = baseSystemConfigDTO.getUrl() + "/#/share/shareReportTestPlan?type=" +
				(report.getIntegrated() ? TestPlanConstants.TEST_PLAN_TYPE_GROUP : TestPlanConstants.TEST_PLAN_TYPE_PLAN) + "&shareId=" + shareInfo.getId();
		return TestPlanMessageDTO.builder()
				.num(testPlan.getNum().toString()).name(testPlan.getName()).status(TestPlanStatus.getI18nText(statistics.getStatus())).type(testPlan.getType()).tags(testPlan.getTags())
				.createUser(testPlan.getCreateUser()).createTime(testPlan.getCreateTime()).updateUser(testPlan.getUpdateUser()).updateTime(testPlan.getUpdateTime())
				.plannedStartTime(testPlan.getPlannedStartTime()).plannedEndTime(testPlan.getPlannedEndTime())
				.actualStartTime(testPlan.getActualStartTime()).actualEndTime(testPlan.getActualEndTime())
				.description(testPlan.getDescription()).reportName(report.getName()).reportUrl(reportUrl).reportShareUrl(reportShareUrl)
				.startTime(report.getStartTime()).endTime(report.getEndTime()).execStatus(TestPlanStatus.getI18nText(report.getExecStatus()))
				.resultStatus(TestPlanStatus.getI18nText(report.getResultStatus())).passRate(report.getPassRate()).passThreshold(report.getPassThreshold()).executeRate(report.getExecuteRate())
				.build();
	}
}
