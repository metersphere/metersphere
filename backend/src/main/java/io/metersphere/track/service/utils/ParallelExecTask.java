/**
 *
 */
package io.metersphere.track.service.utils;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;

import java.util.concurrent.Callable;

public class ParallelExecTask<T> implements Callable<T> {
    private RunTestPlanRequest request;
    private PerformanceTestService performanceTestService;
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    public ParallelExecTask(PerformanceTestService performanceTestService, TestPlanLoadCaseMapper testPlanLoadCaseMapper, RunTestPlanRequest request) {
        this.performanceTestService = performanceTestService;
        this.testPlanLoadCaseMapper = testPlanLoadCaseMapper;
        this.request = request;
    }

    @Override
    public T call() {
        try {
            String reportId = performanceTestService.run(request);
            TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
            testPlanLoadCase.setId(request.getTestPlanLoadId());
            testPlanLoadCase.setLoadReportId(reportId);
            testPlanLoadCase.setStatus("run");
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
            return (T) reportId;

        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
