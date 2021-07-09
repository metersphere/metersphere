package io.metersphere.api.jmeter;


import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.api.service.MsResultService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.*;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    private final static String THREAD_SPLIT = " ";

    private final static String ID_SPLIT = "-";

    private final List<SampleResult> queue = new ArrayList<>();

    private TestResultService testResultService;

    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;

    private MsResultService resultService;

    public String runMode = ApiRunMode.RUN.name();

    // 测试ID
    private String testId;

    private String debugReportId;

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setParam(context);
        testResultService = CommonBeanFactory.getBean(TestResultService.class);
        if (testResultService == null) {
            LogUtil.error("testResultService is required");
        }
        resultService = CommonBeanFactory.getBean(MsResultService.class);
        if (resultService == null) {
            LogUtil.error("MsResultService is required");
        }

        apiEnvironmentRunningParamService = CommonBeanFactory.getBean(ApiEnvironmentRunningParamService.class);
        if (apiEnvironmentRunningParamService == null) {
            LogUtil.error("apiEnvironmentRunningParamService is required");
        }
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        testResult.setConsole(resultService.getJmeterLogger(testId, true));
        testResult.setTotal(0);
        // 一个脚本里可能包含多个场景(ThreadGroup)，所以要区分开，key: 场景Id
        final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
        queue.forEach(result -> {
            // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
            if (StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
                String evnStr = result.getResponseDataAsString();
                apiEnvironmentRunningParamService.parseEvn(evnStr);
            } else {
                resultService.formatTestResult(testResult, scenarios, result);
            }
        });
        queue.clear();
        super.teardownTest(context);
        testResult.getScenarios().addAll(scenarios.values());
        testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
        testResultService.saveResult(testResult, this.runMode, this.debugReportId, this.testId);
    }

    private void setParam(BackendListenerContext context) {
        this.testId = context.getParameter(TEST_ID);
        this.runMode = context.getParameter("runMode");
        this.debugReportId = context.getParameter("debugReportId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
        }
    }
}
