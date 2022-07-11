package io.metersphere.api.exec.perf;

import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfQueueService {
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private PerfModeExecService perfModeExecService;
    @Resource
    protected ApiExecutionQueueMapper queueMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    /**
     * 性能测试监听检查
     *
     * @param loadTestReport
     */
    public void queueNext(LoadTestReport loadTestReport) {
        LoggerUtil.info("进入结果处理监听", loadTestReport.getId());

        ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
        detailExample.createCriteria().andReportIdEqualTo(loadTestReport.getId());
        List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(detailExample);
        executionQueueDetailMapper.deleteByExample(detailExample);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<String> ids = details.stream().map(ApiExecutionQueueDetail::getQueueId).collect(Collectors.toList());
        ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
        queueExample.createCriteria().andIdIn(ids);
        List<ApiExecutionQueue> queues = queueMapper.selectByExample(queueExample);

        List<String> testPlanReportIds = queues.stream().map(ApiExecutionQueue::getReportId).collect(Collectors.toList());
        for (String testPlanReportId : testPlanReportIds) {
            LoggerUtil.info("处理测试计划报告状态", loadTestReport.getId());
            apiExecutionQueueService.testPlanReportTestEnded(testPlanReportId);
        }

        for (ApiExecutionQueueDetail detail : details) {
            // 更新测试计划关联数据状态
            TestPlanLoadCaseWithBLOBs loadCase = new TestPlanLoadCaseWithBLOBs();
            loadCase.setId(loadTestReport.getTestId());
            loadCase.setStatus(TestPlanLoadCaseStatus.success.name());
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(loadCase);

            // 检查队列是否已空
            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(detail.getQueueId());
            long count = executionQueueDetailMapper.countByExample(queueDetailExample);
            // 最后一个执行节点，删除执行链
            if (count == 0) {
                queueMapper.deleteByPrimaryKey(detail.getQueueId());
                continue;
            }
            // 获取串行下一个执行节点
            DBTestQueue executionQueue = apiExecutionQueueService.handleQueue(detail.getQueueId(), detail.getTestId());
            if (executionQueue != null && executionQueue.getDetail() != null
                    && StringUtils.isNotEmpty(executionQueue.getDetail().getTestId()) &&
                    StringUtils.equals(executionQueue.getRunMode(), RunModeConstants.SERIAL.toString())) {

                LoggerUtil.info("获取下一个执行资源：" + executionQueue.getDetail().getTestId(), loadTestReport.getId());
                RunTestPlanRequest request = new RunTestPlanRequest();
                request.setTestPlanLoadId(executionQueue.getDetail().getTestId());
                request.setReportId(executionQueue.getDetail().getReportId());
                try {
                    perfModeExecService.serial(request);
                } catch (Exception e) {
                    if (BooleanUtils.isTrue(executionQueue.getFailure()) && StringUtils.isNotEmpty(executionQueue.getCompletedReportId())) {
                        LoggerUtil.info("失败停止处理：" + request.getId(), request.getReportId());
                        continue;
                    }
                    LoggerUtil.error("执行异常", executionQueue.getDetail().getTestId(), e);
                    executionQueueDetailMapper.deleteByExample(detailExample);
                    // 异常执行下一个
                    DBTestQueue next = apiExecutionQueueService.handleQueue(executionQueue.getId(), executionQueue.getDetail().getTestId());
                    if (next != null) {
                        LoadTestReport report = new LoadTestReport();
                        report.setId(next.getReportId());
                        this.queueNext(report);
                    }
                }
            }
        }
    }
}
