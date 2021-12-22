/**
 *
 */
package io.metersphere.track.service.utils;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;

import java.util.concurrent.Callable;

public class SerialExecTask<T> implements Callable<T> {
    private RunTestPlanRequest request;
    private PerformanceTestService performanceTestService;
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    private LoadTestReportMapper loadTestReportMapper;

    public SerialExecTask(PerformanceTestService performanceTestService, TestPlanLoadCaseMapper testPlanLoadCaseMapper,LoadTestReportMapper loadTestReportMapper, RunTestPlanRequest request) {
        this.performanceTestService = performanceTestService;
        this.testPlanLoadCaseMapper = testPlanLoadCaseMapper;
        this.loadTestReportMapper = loadTestReportMapper;
        this.request = request;
    }

    @Override
    public T call() {
        try {
            // 串行，开启轮询等待
            String reportId = performanceTestService.run(request);
            TestPlanLoadCaseWithBLOBs testPlanLoadCase = new TestPlanLoadCaseWithBLOBs();
            testPlanLoadCase.setId(request.getTestPlanLoadId());
            testPlanLoadCase.setLoadReportId(reportId);
            testPlanLoadCase.setStatus("run");
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
            LoadTestReportWithBLOBs report = null;
            // 轮询查看报告状态，最多200次，防止死循环
            int index = 1;
            while (index < 200) {
                Thread.sleep(3000);
                index++;
                report = loadTestReportMapper.selectByPrimaryKey(reportId);
                if (report != null && (report.getStatus().equals("Completed") || report.getStatus().equals("Error") || report.getStatus().equals("Saved"))) {
                    break;
                }
            }
            return (T) report;

        } catch (Exception ex) {
            LogUtil.error(ex);
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}
