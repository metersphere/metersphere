package io.metersphere.job.sechedule;

import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.SchedulePlanScenarioExecuteRequest;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunTestPlanRequest;
import io.metersphere.track.service.*;
import org.quartz.*;

import java.util.*;

/**
 * 情景测试Job
 *
 * @author song.tianyang
 * @Date 2020/12/22 2:59 下午
 * @Description
 */
public class TestPlanTestJob extends MsScheduleJob {
    private String projectID;
    private Map<String,String> planScenarioIdMap;
    private Map<String,String> apiTestCaseIdMap;
    private Map<String,String> performanceIdMap;

    private PerformanceTestService performanceTestService;
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    private TestPlanApiCaseService testPlanApiCaseService;
    private ApiTestCaseService apiTestCaseService;
    private TestPlanReportService testPlanReportService;
    private TestPlanLoadCaseService testPlanLoadCaseService;
    private TestPlanService testPlanService;

    public TestPlanTestJob() {
        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);
        this.testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);
        this.testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
        this.apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        this.testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
        this.testPlanLoadCaseService = CommonBeanFactory.getBean(TestPlanLoadCaseService.class);
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

        planScenarioIdMap = new LinkedHashMap<>();
        apiTestCaseIdMap = new LinkedHashMap<>();
        performanceIdMap = new LinkedHashMap<>();

        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanScenarioCaseService.getCasesByPlanId(this.resourceId);
        for (TestPlanApiScenario model :testPlanApiScenarioList) {
            planScenarioIdMap.put(model.getApiScenarioId(),model.getId());
        }
        List<TestPlanApiCase> testPlanApiCaseList = testPlanApiCaseService.getCasesByPlanId(this.resourceId);
        for (TestPlanApiCase model :
                testPlanApiCaseList) {
            apiTestCaseIdMap.put(model.getApiCaseId(),model.getId());
        }

        LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
        loadCaseRequest.setTestPlanId(this.resourceId);
        loadCaseRequest.setProjectId(this.projectID);
        List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = testPlanLoadCaseService.list(loadCaseRequest);
        for (TestPlanLoadCaseDTO dto : testPlanLoadCaseDTOList) {
            performanceIdMap.put(dto.getId(),dto.getLoadCaseId());
        }

        businessExecute(context);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        LogUtil.info("-------------- start testplan schedule ----------");
        //首先创建testPlanReport，然后返回的ID重新赋值为resourceID，作为后续的参数
        TestPlanReport testPlanReport = testPlanReportService.genTestPlanReport(this.resourceId,this.userId,ReportTriggerMode.SCHEDULE.name());
        //执行接口案例任务
        for (Map.Entry<String,String> entry: this.apiTestCaseIdMap.entrySet()) {
            String apiCaseID = entry.getKey();
            String planCaseID = entry.getValue();
            ApiTestCaseWithBLOBs blobs = apiTestCaseService.get(apiCaseID);
            //需要更新这里来保证PlanCase的状态能正常更改
            apiTestCaseService.run(blobs,UUID.randomUUID().toString(),testPlanReport.getId(),this.resourceId,ApiRunMode.SCHEDULE_API_PLAN.name());
        }
        LogUtil.info("-------------- testplan schedule ---------- api case over -----------------");
        //执行场景执行任务
        SchedulePlanScenarioExecuteRequest scenarioRequest = new SchedulePlanScenarioExecuteRequest();
        String senarionReportID = UUID.randomUUID().toString();
        scenarioRequest.setId(senarionReportID);
        scenarioRequest.setReportId(senarionReportID);
        scenarioRequest.setProjectId(projectID);
        scenarioRequest.setTriggerMode(ReportTriggerMode.SCHEDULE.name());
        scenarioRequest.setExecuteType(ExecuteType.Saved.name());
        Map<String, Map<String,String>> testPlanScenarioIdMap = new HashMap<>();
        testPlanScenarioIdMap.put(resourceId, this.planScenarioIdMap);
        scenarioRequest.setTestPlanScenarioIDMap(testPlanScenarioIdMap);
        scenarioRequest.setReportUserID(this.userId);
        scenarioRequest.setTestPlanID(this.resourceId);
        scenarioRequest.setRunMode(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
        scenarioRequest.setTestPlanReportId(testPlanReport.getId());
        testPlanService.runScenarioCase(scenarioRequest);
        LogUtil.info("-------------- testplan schedule ---------- scenario case over -----------------");

        //执行性能测试任务
        List<String> performaneReportIDList = new ArrayList<>();
        for (Map.Entry<String,String> entry: this.performanceIdMap.entrySet()) {
            String id = entry.getKey();
            String caseID = entry.getValue();
            RunTestPlanRequest performanceRequest = new RunTestPlanRequest();
            performanceRequest.setId(caseID);
            performanceRequest.setTestPlanLoadId(caseID);
            performanceRequest.setTriggerMode(ReportTriggerMode.TEST_PLAN_SCHEDULE.name());

            String reportId = null;
            try {
                reportId = performanceTestService.run(performanceRequest);
                if(reportId!=null){
                    performaneReportIDList.add(reportId);

                    TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
                    testPlanLoadCase.setId(performanceRequest.getTestPlanLoadId());
                    testPlanLoadCase.setLoadReportId(reportId);
                    testPlanLoadCaseService.update(testPlanLoadCase);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(!performaneReportIDList.isEmpty()){
            //性能测试时保存性能测试报告ID，在结果返回时用于捕捉并进行
            testPlanReportService.updatePerformanceInfo(testPlanReport,performaneReportIDList,ReportTriggerMode.SCHEDULE.name());
        }

    }

    public static JobKey getJobKey(String testId) {
        return new JobKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }

    public static TriggerKey getTriggerKey(String testId) {
        return new TriggerKey(testId, ScheduleGroup.TEST_PLAN_TEST.name());
    }
}
