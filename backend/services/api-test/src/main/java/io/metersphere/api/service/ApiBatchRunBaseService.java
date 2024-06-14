package io.metersphere.api.service;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CommonConstants;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiBatchRunBaseService {
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    /**
     * 初始化执行队列
     *
     * @param resourceIds
     * @param runModeConfig
     * @return
     */
    public ExecutionQueue initExecutionqueue(List<String> resourceIds, ApiRunModeConfigDTO runModeConfig, String resourceType, String userId) {
        return initExecutionqueue(resourceIds, runModeConfig, resourceType, null, userId);
    }

    /**
     * 初始化执行队列
     *
     * @param resourceIds
     * @param runModeConfig
     * @return
     */
    public ExecutionQueue initExecutionqueue(List<String> resourceIds, ApiRunModeConfigDTO runModeConfig, String resourceType, String parentQueueId, String userId) {
        ExecutionQueue queue = getExecutionQueue(runModeConfig, resourceType, userId);
        queue.setParentQueueId(parentQueueId);
        List<ExecutionQueueDetail> queueDetails = getExecutionQueueDetails(resourceIds);
        apiExecutionQueueService.insertQueue(queue, queueDetails);
        return queue;
    }

    /**
     * 初始化执行队列
     *
     * @param resourceIds
     * @return
     */
    public ExecutionQueue initExecutionqueue(List<String> resourceIds, String resourceType, String userId) {
        ExecutionQueue queue = getExecutionQueue(null, resourceType, userId);
        List<ExecutionQueueDetail> queueDetails = getExecutionQueueDetails(resourceIds);
        apiExecutionQueueService.insertQueue(queue, queueDetails);
        return queue;
    }

    public List<ExecutionQueueDetail> getExecutionQueueDetails(List<String> resourceIds) {
        List<ExecutionQueueDetail> queueDetails = new ArrayList<>();
        AtomicInteger sort = new AtomicInteger(1);
        for (String resourceId : resourceIds) {
            ExecutionQueueDetail queueDetail = new ExecutionQueueDetail();
            queueDetail.setResourceId(resourceId);
            queueDetail.setSort(sort.getAndIncrement());
            queueDetails.add(queueDetail);
        }
        return queueDetails;
    }

    private ExecutionQueue getExecutionQueue(ApiRunModeConfigDTO runModeConfig, String resourceType, String userId) {
        ExecutionQueue queue = new ExecutionQueue();
        queue.setQueueId(UUID.randomUUID().toString());
        queue.setRunModeConfig(runModeConfig);
        queue.setResourceType(resourceType);
        queue.setCreateTime(System.currentTimeMillis());
        queue.setUserId(userId);
        return queue;
    }

    public ApiScenarioReport computeRequestRate(ApiScenarioReport report , long total) {
        // 计算各个概率
        double successRate = calculateRate(report.getSuccessCount(), total);
        double errorRate = calculateRate(report.getErrorCount(), total);
        double pendingRate = calculateRate(report.getPendingCount(), total);
        double fakeErrorRate = calculateRate(report.getFakeErrorCount(), total);

        // 计算总和
        double sum = successRate + errorRate + pendingRate + fakeErrorRate;

        LogUtils.info("偏移总量重新计算", sum);

        // 避免分母为零
        double adjustment = sum > 0 ? 1.0 / sum : 0.0;

        // 调整概率，使总和精确为100%
        successRate *= adjustment;
        errorRate *= adjustment;
        pendingRate *= adjustment;
        fakeErrorRate *= adjustment;

        report.setRequestPassRate(formatRate(successRate));
        report.setRequestErrorRate(formatRate(errorRate));
        report.setRequestPendingRate(formatRate(pendingRate));
        report.setRequestFakeErrorRate(formatRate(fakeErrorRate));

        return report;
    }

    // 计算概率
    private static double calculateRate(long count, double total) {
        return total > 0 ? count / total : 0.0;
    }

    // 格式化概率，保留两位小数
    private static String formatRate(double rate) {
        return String.format("%.2f", rate * 100);
    }

    public TaskInfo setBatchRunTaskInfoParam(ApiRunModeConfigDTO runModeConfig, TaskInfo taskInfo) {
        taskInfo.setSaveResult(true);
        taskInfo.setRealTime(false);
        taskInfo.setNeedParseScript(true);
        taskInfo.setRunModeConfig(runModeConfig);
        return taskInfo;
    }

    public boolean isParallel(String runMode) {
        return StringUtils.equals(runMode, ApiBatchRunMode.PARALLEL.name());
    }

    public String getEnvId(ApiRunModeConfigDTO runModeConfig, String caseEnvId) {
        if (StringUtils.isBlank(runModeConfig.getEnvironmentId()) || StringUtils.equals(runModeConfig.getEnvironmentId(), CommonConstants.DEFAULT_NULL_VALUE)) {
            return caseEnvId;
        }
        return runModeConfig.getEnvironmentId();
    }
}
