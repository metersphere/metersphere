package io.metersphere.api.service;

import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        ExecutionQueue queue = getExecutionQueue(runModeConfig, resourceType, userId);
        List<ExecutionQueueDetail> queueDetails = new ArrayList<>();
        AtomicInteger sort = new AtomicInteger(1);
        for (String resourceId : resourceIds) {
            ExecutionQueueDetail queueDetail = new ExecutionQueueDetail();
            queueDetail.setResourceId(resourceId);
            queueDetail.setSort(sort.getAndIncrement());
            queueDetails.add(queueDetail);
        }
        apiExecutionQueueService.insertQueue(queue, queueDetails);
        return queue;
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
}
