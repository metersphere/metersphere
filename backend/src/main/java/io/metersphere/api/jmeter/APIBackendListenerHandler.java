package io.metersphere.api.jmeter;


import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.service.MsResultService;
import io.metersphere.api.service.TestResultService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 获取结果和数据库操作分离
 * 减少占用的数据库连接
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class APIBackendListenerHandler {

    @Resource
    private TestResultService testResultService;
    @Resource
    private MsResultService resultService;

    public void handleTeardownTest(List<SampleResult> queue, String runMode, String testId, String debugReportId, String amassReport) throws Exception {
        try {
            TestResult testResult = new TestResult();
            testResult.setTestId(testId);
            testResult.setSetReportId(amassReport);
            MessageCache.runningEngine.remove(testId);
            testResult.setTotal(0);
            List<String> environmentList = new ArrayList<>();
            // 一个脚本里可能包含多个场景(ThreadGroup)，所以要区分开，key: 场景Id
            final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
            queue.forEach(result -> {
                // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
                if (StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
                    String evnStr = result.getResponseDataAsString();
                    environmentList.add(evnStr);
                } else {
                    resultService.formatTestResult(testResult, scenarios, result);
                }
            });
            queue.clear();
            testResult.getScenarios().addAll(scenarios.values());
            testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
            testResult.setConsole(resultService.getJmeterLogger(testId, true));
            //处理环境参数
            ExecutedHandleSingleton.parseEnvironment(environmentList);
            testResultService.saveResult(testResult, runMode, debugReportId, testId);
            // 清除已经中断的过程数据
            if (!MessageCache.reportCache.containsKey(testId) && resultService.getProcessCache().containsKey(testId)) {
                resultService.getProcessCache().remove(testId);
            }
            if (StringUtils.isNotEmpty(testId)) {
                MessageCache.executionQueue.remove(testId);
            }
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(amassReport) && MessageCache.cache.get(amassReport) != null
                    && StringUtils.isNotEmpty(testId)
                    && CollectionUtils.isNotEmpty(MessageCache.cache.get(amassReport).getReportIds())) {
                MessageCache.cache.get(amassReport).getReportIds().remove(testId);
            }
        }
    }
}
