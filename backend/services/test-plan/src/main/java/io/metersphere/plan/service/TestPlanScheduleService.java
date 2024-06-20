package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.job.TestPlanScheduleJob;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.request.ScheduleConfig;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.metersphere.system.schedule.ScheduleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanScheduleService {

	@Resource
	private TestPlanMapper testPlanMapper;
	@Resource
	private ScheduleService scheduleService;

	public String scheduleConfig(BaseScheduleConfigRequest request, String operator) {
		TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getResourceId());
		if (testPlan == null) {
			throw new MSException(Translator.get("test_plan.not.exist"));
		}
		ScheduleConfig scheduleConfig = ScheduleConfig.builder()
				.resourceId(testPlan.getId())
				.key(testPlan.getId())
				.projectId(testPlan.getProjectId())
				.name(testPlan.getName())
				.enable(request.isEnable())
				.cron(request.getCron())
				.resourceType(ScheduleResourceType.TEST_PLAN.name())
				.config(JSON.toJSONString(request.getRunConfig()))
				.build();

		if (request.isEnable() && StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
			//配置开启的测试计划组定时任务，要将组下的所有测试计划定时任务都关闭掉
			TestPlanExample example = new TestPlanExample();
			example.createCriteria().andGroupIdEqualTo(testPlan.getId()).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
			example.setOrderByClause("pos asc");
			List<TestPlan> children = testPlanMapper.selectByExample(example);
			for (TestPlan child : children) {
				scheduleService.updateIfExist(child.getId(), false, TestPlanScheduleJob.getJobKey(testPlan.getId()),
						TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
						TestPlanScheduleJob.class, operator);
			}
		}

		return scheduleService.scheduleConfig(
				scheduleConfig,
				TestPlanScheduleJob.getJobKey(testPlan.getId()),
				TestPlanScheduleJob.getTriggerKey(testPlan.getId()),
				TestPlanScheduleJob.class,
				operator);
	}
}
