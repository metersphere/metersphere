package io.metersphere.api.service;

import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.queue.ApiExecutionSetService;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CommonConstants;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.mapper.ExecTaskMapper;
import io.metersphere.system.mapper.ExtExecTaskItemMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiBatchRunBaseService {
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ApiExecutionSetService apiExecutionSetService;

    public static final int BATCH_TASK_ITEM_SIZE = 500;

    public static final int SELECT_BATCH_SIZE = 200;

    /**
     * 初始化执行队列
     *
     * @param resourceIds
     * @param runModeConfig
     * @return
     */
    public ExecutionQueue initExecutionqueue(String queueId, List<String> resourceIds, ApiRunModeConfigDTO runModeConfig, String resourceType, String parentQueueId, String userId, boolean isRerun) {
        ExecutionQueue queue = getExecutionQueue(runModeConfig, resourceType, userId);
        if (StringUtils.isNotBlank(queueId)) {
            queue.setQueueId(queueId);
        }
        queue.setParentQueueId(parentQueueId);
        queue.setRerun(isRerun);
        List<ExecutionQueueDetail> queueDetails = getExecutionQueueDetailsByIds(resourceIds);
        apiExecutionQueueService.insertQueue(queue, queueDetails);
        return queue;
    }

    public ExecutionQueue initExecutionQueue(String taskId, ApiRunModeConfigDTO runModeConfig, String resourceType, String parentQueueId, String userId) {
        return initExecutionQueue(taskId, null, runModeConfig, resourceType, parentQueueId, null, userId);
    }

    /**
     * 初始化执行队列
     *
     * @param runModeConfig
     * @return
     */
    public ExecutionQueue initExecutionQueue(String taskId, String queueId,
                                             ApiRunModeConfigDTO runModeConfig,
                                             String resourceType,
                                             String parentQueueId,
                                             String parentSetId,
                                             String userId) {
        ExecutionQueue queue = getExecutionQueue(runModeConfig, resourceType, taskId, userId);
        if (StringUtils.isNotBlank(queueId)) {
            queue.setQueueId(queueId);
        }
        queue.setParentQueueId(parentQueueId);
        queue.setParentSetId(parentSetId);
        apiExecutionQueueService.insertQueue(queue);
        return queue;
    }

    /**
     * 初始化执行队列
     *
     * @param
     * @return
     */
    public void initExecutionQueueDetails(String queueId, List<ExecTaskItem> execTaskItems) {
        List<ExecutionQueueDetail> queueDetails = getExecutionQueueDetails(execTaskItems);
        apiExecutionQueueService.insertQueueDetail(queueId, queueDetails);
    }

    public List<ExecutionQueueDetail> getExecutionQueueDetailsByIds(List<String> resourceIds) {
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

    public List<ExecutionQueueDetail> getExecutionQueueDetails(List<ExecTaskItem> execTaskItems) {
        List<ExecutionQueueDetail> queueDetails = new ArrayList<>();
        AtomicInteger sort = new AtomicInteger(1);
        execTaskItems.forEach(execTaskItem -> {
            ExecutionQueueDetail queueDetail = new ExecutionQueueDetail();
            queueDetail.setResourceId(execTaskItem.getResourceId());
            queueDetail.setTaskItemId(execTaskItem.getId());
            queueDetail.setSort(sort.getAndIncrement());
            queueDetails.add(queueDetail);
        });
        return queueDetails;
    }

    public ExecutionQueue getExecutionQueue(ApiRunModeConfigDTO runModeConfig, String resourceType, String userId) {
        return getExecutionQueue(runModeConfig, resourceType, null, userId);
    }

    public ExecutionQueue getExecutionQueue(ApiRunModeConfigDTO runModeConfig, String resourceType, String taskId, String userId) {
        ExecutionQueue queue = new ExecutionQueue();
        queue.setQueueId(UUID.randomUUID().toString());
        queue.setRunModeConfig(runModeConfig);
        queue.setResourceType(resourceType);
        queue.setCreateTime(System.currentTimeMillis());
        queue.setUserId(userId);
        queue.setTaskId(taskId);
        return queue;
    }

    public ApiScenarioReport computeRequestRate(ApiScenarioReport report, long total) {
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

    public void updateTaskCompletedStatus(String taskId) {
        updateTaskCompletedStatus(taskId, null);
    }

    public void updateTaskCompletedStatus(String taskId, String result) {
        // 删除执行缓存
        removeRunningTaskCache(taskId);
        ExecTask originExecTask = execTaskMapper.selectByPrimaryKey(taskId);
        // 出现异常，导致任务没有开始，则不更新任务状态
        if (!StringUtils.equals(originExecTask.getStatus(), ExecStatus.PENDING.name())) {
            // 更新任务状态
            ExecTask execTask = new ExecTask();
            execTask.setEndTime(System.currentTimeMillis());
            execTask.setId(taskId);
            execTask.setStatus(ExecStatus.COMPLETED.name());
            if (StringUtils.isNotBlank(result)) {
                execTask.setResult(result);
            } else {
                if (extExecTaskItemMapper.hasErrorItem(taskId)) {
                    execTask.setResult(ResultStatus.ERROR.name());
                } else if (extExecTaskItemMapper.hasFakeErrorItem(taskId)) {
                    execTask.setResult(ResultStatus.FAKE_ERROR.name());
                } else {
                    execTask.setResult(ResultStatus.SUCCESS.name());
                }
            }
            execTaskMapper.updateByPrimaryKeySelective(execTask);
        }
    }

    /**
     * 清理正在运行的任务缓存
     *
     * @param taskId
     */
    public void removeRunningTaskCache(String taskId) {
        stringRedisTemplate.delete(CommonConstants.RUNNING_TASK_PREFIX + taskId);
    }

    public void parallelBatchExecute(TaskBatchRequestDTO taskRequest,
                         ApiRunModeConfigDTO runModeConfig,
                         Map<String, String> resourceExecTaskItemMap) {

        int count = 0;
        List<TaskItem> taskItems = new ArrayList<>(BATCH_TASK_ITEM_SIZE);
        for (String resourceId : resourceExecTaskItemMap.keySet()) {

            // 如果是集成报告则生成唯一的虚拟ID，非集成报告使用单用例的报告ID
            String reportId = runModeConfig.isIntegratedReport() ? runModeConfig.getCollectionReport().getReportId() : null;
            TaskItem taskItem = apiExecuteService.getTaskItem(reportId, resourceId);
            taskItem.setId(resourceExecTaskItemMap.get(resourceId));
            taskItem.setRequestCount(1L);
            taskItems.add(taskItem);

            count++;
            if (count >= BATCH_TASK_ITEM_SIZE) {
                taskRequest.setTaskItems(taskItems);
                apiExecuteService.batchExecute(taskRequest);
                taskRequest.setTaskItems(null);
                count = 0;
                taskItems.clear();
            }
        }

        if (count > 0) {
            taskRequest.setTaskItems(taskItems);
            try {
                apiExecuteService.batchExecute(taskRequest);
            } catch (Exception e) {
                // 执行失败，删除执行集合中的任务项
                List<String> taskItemIds = taskRequest.getTaskItems().stream().map(TaskItem::getId).toList();
                apiExecutionSetService.removeItems(taskRequest.getTaskInfo().getSetId(), taskItemIds);
                LogUtils.error(e);
            }
        }
    }

    public List<ExecTaskItem> getExecTaskItemByTaskIdAndCollectionId(String taskId, String collectionId, boolean rerun) {
        List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.selectExecInfoByTaskIdAndCollectionId(taskId, collectionId, rerun)
                .stream().sorted(Comparator.comparing(ExecTaskItem::getId)).toList();
        return execTaskItems;
    }

    /**
     * 初始化队列项
     * @param queue
     * @param execTaskItems
     */
    public void initQueueDetail(ExecutionQueue queue, List<ExecTaskItem> execTaskItems) {
        SubListUtils.dealForSubList(execTaskItems, ApiBatchRunBaseService.BATCH_TASK_ITEM_SIZE,
                subExecTaskItems -> initExecutionQueueDetails(queue.getQueueId(), subExecTaskItems));
    }

    public String getIntegratedReportId(ExecTask execTask) {
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo(execTask.getId());
        List<ApiReportRelateTask> apiReportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        String reportId = IDGenerator.nextStr();
        if (CollectionUtils.isNotEmpty(apiReportRelateTasks)) {
            reportId = apiReportRelateTasks.getFirst().getReportId();
        }
        return reportId;
    }
}
