package io.metersphere.plan.job;

import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.plan.dto.ExecutionWay;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sechedule.MsScheduleJob;
import io.metersphere.service.BaseUserService;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.quartz.*;

/**
 * 情景测试Job
 *
 * @author song.tianyang
 * @Date 2020/12/22 2:59 下午
 * @Description
 */
public class TestPlanTestJob extends MsScheduleJob {
    private String projectID;

    private TestPlanService testPlanService;

    private BaseUserService baseUserService;

    private static DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();

    public TestPlanTestJob() {
        this.testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
        this.baseUserService = CommonBeanFactory.getBean(BaseUserService.class);
    }

    /**
     * 情景部分的准备工作
     *
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        this.projectID = jobDataMap.getString("projectId");

        // 业务中涉及远程调用, 需在定时任务中获取subject.
        ThreadContext.bind(defaultSecurityManager);
        businessExecute(context);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String config = jobDataMap.getString("config");

        String runResourceId = this.resourceId;
        String runProjectId = this.projectID;
        String runUserId = this.userId;

        // 定时任务指定调用微服务的user
        HttpHeaderUtils.runAsUser(baseUserService.getUserDTO(runUserId));
        testPlanService.runTestPlanBySchedule(runResourceId, runProjectId, runUserId, ReportTriggerMode.SCHEDULE.name(), null, ExecutionWay.RUN.name(), config);
        HttpHeaderUtils.clearUser();
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }
}
