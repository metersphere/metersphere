package io.metersphere.track.service;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LoadReportStatusTask {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    private boolean isRunning = false;

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }

    public void registerReportIsEndTask(String id, String reportId) {
        isRunning = true;
        // todo 手动创建线程池
        executorService.submit(() -> {
            while (isRunning) {
                LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
                if (StringUtils.equalsAny(report.getStatus(), PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                    updateLoadCaseStatus(id, report.getStatus());
                    return;
                }
                try {
                    //查询定时任务是否关闭
                    Thread.sleep(1000 * 10);// 检查 loadtest 的状态
                } catch (InterruptedException e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        });
    }

    private void updateLoadCaseStatus(String testPlanLoadCaseId, String status) {
        TestPlanLoadCase testPlanLoadCase = new TestPlanLoadCase();
        testPlanLoadCase.setId(testPlanLoadCaseId);
        String result = "";
        if (StringUtils.equals(PerformanceTestStatus.Error.name(), status)) {
            result = "error";
        }
        if (StringUtils.equals(PerformanceTestStatus.Completed.name(), status)) {
            result = "success";
        }
        testPlanLoadCase.setStatus(result);
        testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
    }
}
