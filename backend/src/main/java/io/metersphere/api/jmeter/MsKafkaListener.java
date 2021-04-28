package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsKafkaListener {
    public static final String TOPICS = "ms-api-exec-topic";
    public static final String CONSUME_ID = "ms-api-exec-consume";

    @KafkaListener(id = CONSUME_ID, topics = TOPICS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LogUtil.info("接收到执行结果开始存储");
        this.save(record.value());
        LogUtil.info("执行内容存储结束");
    }

    @Resource
    private APITestService apiTestService;
    @Resource
    private APIReportService apiReportService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

    private TestResult formatResult(String result) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (StringUtils.isNotEmpty(result)) {
                TestResult element = mapper.readValue(result, new TypeReference<TestResult>() {
                });
                return element;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage());
        }
        return null;
    }

    private void save(String execResult) {
        TestResult testResult = this.formatResult(execResult);
        ApiTestReport report = null;
        String reportUrl = null;
        // 这部分后续优化只留 DEFINITION 和 SCENARIO 两部分
        if (StringUtils.equals(testResult.getRunMode(), ApiRunMode.DEFINITION.name())) {
            // 调试操作，不需要存储结果
            apiDefinitionService.addResult(testResult);
            if (!testResult.isDebug()) {
                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name());
            }
        } else if (StringUtils.equals(testResult.getRunMode(), ApiRunMode.JENKINS.name())) {
            apiDefinitionService.addResult(testResult);
            apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name());

        } else if (StringUtils.equals(testResult.getRunMode(), ApiRunMode.JENKINS_API_PLAN.name())) {
            apiDefinitionService.addResult(testResult);
            apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.API_PLAN.name());
            ApiDefinitionExecResult result = apiDefinitionService.getResultByJenkins(testResult.getTestId(), ApiRunMode.API_PLAN.name());
            if (result != null) {
                report = new ApiTestReport();
                report.setStatus(result.getStatus());
                report.setId(result.getId());
                report.setTriggerMode(ApiRunMode.API.name());
                report.setName(apiDefinitionService.getApiCaseInfo(testResult.getTestId()).getName());
            }
        } else if (StringUtils.equalsAny(testResult.getRunMode(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name())) {
            apiDefinitionService.addResult(testResult);
            //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
            if (StringUtils.equalsAny(testResult.getRunMode(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
                apiDefinitionExecResultService.saveApiResultByScheduleTask(testResult, ApiRunMode.SCHEDULE_API_PLAN.name());
                List<String> testPlanReportIdList = new ArrayList<>();
                testPlanReportIdList.add(testResult.getTestId());
                //  更新每个测试计划的状态
                for (String testPlanReportId : testPlanReportIdList) {
                    testPlanReportService.checkTestPlanStatus(testPlanReportId);
                }
                testPlanReportService.updateReport(testPlanReportIdList, ApiRunMode.SCHEDULE_API_PLAN.name(), ReportTriggerMode.SCHEDULE.name());
            } else {
                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.API_PLAN.name());
            }
        } else if (StringUtils.equalsAny(testResult.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name())) {
            // 执行报告不需要存储，由用户确认后在存储
            testResult.setTestId(testResult.getTestId());
            ApiScenarioReport scenarioReport = apiScenarioReportService.complete(testResult, testResult.getRunMode());

            report = new ApiTestReport();
            report.setStatus(scenarioReport.getStatus());
            report.setId(scenarioReport.getId());
            report.setTriggerMode(scenarioReport.getTriggerMode());
            report.setName(scenarioReport.getName());

            SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
            assert systemParameterService != null;
            BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
            reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report";

            testResult.setTestId(scenarioReport.getScenarioId());
        } else {
            apiTestService.changeStatus(testResult.getTestId(), APITestStatus.Completed);
            report = apiReportService.getRunningReport(testResult.getTestId());
            apiReportService.complete(testResult, report);
        }

        TestPlanTestCaseService testPlanTestCaseService = CommonBeanFactory.getBean(TestPlanTestCaseService.class);
        List<String> ids = testPlanTestCaseService.getTestPlanTestCaseIds(testResult.getTestId());
        if (ids.size() > 0) {
            try {
                if (StringUtils.equals(APITestStatus.Success.name(), report.getStatus())) {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Pass.name());
                } else {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Failure.name());
                }
            } catch (Exception e) {

            }
        }
        if (report != null) {
            if (StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode()) || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
                sendTask(report, reportUrl, testResult);
            }
        }
    }

    private static void sendTask(ApiTestReport report, String reportUrl, TestResult testResult) {
        if (report == null) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/api/report/view/" + report.getId();
        String url2 = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + report.getId();

        String successContext = "";
        String failedContext = "";
        String subject = "";
        String event = "";
        if (StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode())) {
            successContext = "接口测试 API任务通知:'" + report.getName() + "'执行成功" + "\n" + "【接口定义暂无报告链接】" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测）路径" + url + "\n" + "（新版）接口测试路径" + url2;
            failedContext = "接口测试 API任务通知:'" + report.getName() + "'执行失败" + "\n" + "【接口定义暂无报告链接】" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            successContext = "接口测试定时任务通知:'" + report.getName() + "'执行成功" + "\n" + "【接口定义暂无报告链接】" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            failedContext = "接口测试定时任务通知:'" + report.getName() + "'执行失败" + "\n" + "【接口定义暂无报告链接】" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            subject = Translator.get("task_notification");
        }
        if (StringUtils.equals("Success", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equals("success", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equals("Error", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        if (StringUtils.equals("error", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("testName", report.getName());
        paramMap.put("id", report.getId());
        paramMap.put("type", "api");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("status", report.getStatus());
        NoticeModel noticeModel = NoticeModel.builder()
                .successContext(successContext)
                .successMailTemplate("ApiSuccessfulNotification")
                .failedContext(failedContext)
                .failedMailTemplate("ApiFailedNotification")
                .testId(testResult.getTestId())
                .status(report.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(report.getTriggerMode(), noticeModel);
    }

}
