package io.metersphere.api.jmeter;


import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.api.service.MsResultService;
import io.metersphere.api.service.TestResultService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取结果和数据库操作分离
 * 减少占用的数据库连接
 */
public class APIBackendListenerHandler {

    @Resource
    private TestResultService testResultService;
    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;
    @Resource
    private MsResultService resultService;

    public void handleTeardownTest(List<SampleResult> queue, String runMode, String testId, String debugReportId) throws Exception {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        MessageCache.runningEngine.remove(testId);
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
        testResult.getScenarios().addAll(scenarios.values());
        testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
        testResult.setConsole(resultService.getJmeterLogger(testId, true));
        testResultService.saveResult(testResult, runMode, debugReportId, testId);
        // 清除已经中断的过程数据
        if (!MessageCache.reportCache.containsKey(testId) && resultService.getProcessCache().containsKey(testId)) {
            resultService.getProcessCache().remove(testId);
        }
    }
}
