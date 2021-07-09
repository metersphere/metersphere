package io.metersphere.job.sechedule;

import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.track.service.TestPlanService;
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


    //    private PerformanceTestService performanceTestService;
//    private TestPlanScenarioCaseService testPlanScenarioCaseService;
//    private TestPlanApiCaseService testPlanApiCaseService;
//    private ApiTestCaseService apiTestCaseService;
//    private TestPlanReportService testPlanReportService;
//    private TestPlanLoadCaseService testPlanLoadCaseService;
    private TestPlanService testPlanService;

    public TestPlanTestJob() {
//        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);
//        this.testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);
//        this.testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
//        this.apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
//        this.testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
//        this.testPlanLoadCaseService = CommonBeanFactory.getBean(TestPlanLoadCaseService.class);
        this.testPlanService = CommonBeanFactory.getBean(TestPlanService.class);


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


        businessExecute(context);
    }

    @Override
    void businessExecute(JobExecutionContext context) {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String config = jobDataMap.getString("config");

        testPlanService.run(this.resourceId, this.projectID, this.userId, ReportTriggerMode.SCHEDULE.name(),config);
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }
}
