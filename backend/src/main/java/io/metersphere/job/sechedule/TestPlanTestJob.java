package io.metersphere.job.sechedule;

import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.domain.TestPlanApiScenario;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.request.testplan.RunTestPlanRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.quartz.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 情景测试Job
 * @author song.tianyang
 * @Date 2020/12/22 2:59 下午
 * @Description
 */
public class TestPlanTestJob extends   MsScheduleJob {
    private  String projectID;
    private List<String> scenarioIds;
    private List<String> apiTestCaseIds;
    private List<String> performanceIds;

    private ApiAutomationService apiAutomationService;
    private PerformanceTestService performanceTestService;
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    private TestPlanApiCaseService testPlanApiCaseService;
    private ApiTestCaseService apiTestCaseService;
    private ApiDefinitionService apiDefinitionService;

    public TestPlanTestJob() {
        this.apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);
        this.testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);
        this.testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
        this.apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
    }

    /**
     * 情景部分的准备工作
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        this.projectID = jobDataMap.getString("projectId");
        scenarioIds = new ArrayList<>();

        String testPlanID = jobDataMap.getString("resourceId");
        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanScenarioCaseService.getCasesByPlanId(testPlanID);
        for (TestPlanApiScenario model:
                testPlanApiScenarioList) {
            scenarioIds.add(model.getApiScenarioId());
        }
        List<TestPlanApiCase> testPlanApiCaseList = testPlanApiCaseService.getCasesByPlanId(testPlanID);
        for (TestPlanApiCase model :
                testPlanApiCaseList) {
            apiTestCaseIds.add(model.getApiCaseId());
        }

        businessExecute(context);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        LogUtil.info("-------------- start testplan schedule ----------");
        //执行接口案例任务
        for (String apiCaseID:apiTestCaseIds) {
            ApiTestCaseWithBLOBs blobs = apiTestCaseService.get(apiCaseID);
            String caseReportID = UUID.randomUUID().toString();
            RunDefinitionRequest apiCaseReqeust = new RunDefinitionRequest();
            apiCaseReqeust.setId(apiCaseID);
            apiCaseReqeust.setReportId(caseReportID);
            apiCaseReqeust.setProjectId(projectID);
            apiCaseReqeust.setExecuteType(ExecuteType.Saved.name());
            apiCaseReqeust.setType(ApiRunMode.API_PLAN.name());
            apiDefinitionService.run(apiCaseReqeust,blobs);
        }
        LogUtil.info("-------------- testplan schedule ---------- api case over -----------------");
        //执行场景执行任务
        RunScenarioRequest scenarioRequest = new RunScenarioRequest();
        String senarionReportID = UUID.randomUUID().toString();
        scenarioRequest.setId(senarionReportID);
        scenarioRequest.setReportId(senarionReportID);
        scenarioRequest.setProjectId(projectID);
        scenarioRequest.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        scenarioRequest.setExecuteType(ExecuteType.Saved.name());
        scenarioRequest.setScenarioIds(this.scenarioIds);
        scenarioRequest.setReportUserID(this.userId);
        scenarioRequest.setRunMode(ApiRunMode.SCENARIO_PLAN.name());
        String reportID = apiAutomationService.run(scenarioRequest);
        LogUtil.info("-------------- testplan schedule ---------- scenario case over -----------------");

        //执行性能测试任务 --- 保留，待功能实现后再继续
//        RunTestPlanRequest performanceRequest = new RunTestPlanRequest();
//        performanceRequest.setId(resourceId);
//        performanceRequest.setUserId(userId);
//        performanceRequest.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
//        performanceTestService.run(performanceRequest);
    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }
}
