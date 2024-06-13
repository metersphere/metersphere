package io.metersphere.api.service.queue;

import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ApiExecutionQueueService {

    public static final String QUEUE_PREFIX = "queue:";
    public static final String QUEUE_DETAIL_PREFIX = "queue:detail:";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertQueue(ExecutionQueue queue, List<ExecutionQueueDetail> queues) {
        // 保存队列信息
        redisTemplate.opsForValue().setIfAbsent(QUEUE_PREFIX + queue.getQueueId(), JSON.toJSONString(queue), 1, TimeUnit.DAYS);
        // 保存队列详情信息
        List<String> queueStrItems = queues.stream().map(JSON::toJSONString).toList();
        redisTemplate.opsForList().rightPushAll(QUEUE_DETAIL_PREFIX + queue.getQueueId(), queueStrItems);
        redisTemplate.expire(QUEUE_DETAIL_PREFIX + queue.getQueueId(), 1, TimeUnit.DAYS);
    }

    /**
     * 获取下一个节点
     */
    public ExecutionQueueDetail getNextDetail(String queueId) {
        String queueKey = QUEUE_DETAIL_PREFIX + queueId;
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        String queueDetail = listOps.leftPop(queueKey);
        if (StringUtils.isBlank(queueDetail)) {
            // 重试3次获取
            for (int i = 0; i < 3; i++) {
                queueDetail = redisTemplate.opsForList().leftPop(queueKey);
                if (StringUtils.isNotBlank(queueDetail)) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (StringUtils.isNotBlank(queueDetail)) {
            Long size = size(queueId);
            if (size == null || size == 0) {
                // 最后一个节点清理队列
                deleteQueue(queueId);
            }
            return JSON.parseObject(queueDetail, ExecutionQueueDetail.class);
        }

        // 整体获取完，清理队列
        deleteQueue(queueId);

        return null;
    }

    public void deleteQueue(String queueId) {
        redisTemplate.delete(QUEUE_DETAIL_PREFIX + queueId);
        redisTemplate.delete(QUEUE_PREFIX + queueId);
    }

    /**
     * 获取所有节点
     */
    public List<ExecutionQueueDetail> getDetails(String queueId) {
        String queueKey = QUEUE_DETAIL_PREFIX + queueId;
        List<ExecutionQueueDetail> details = new LinkedList<>();
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        Long listSize = listOps.size(queueKey);
        if (listSize == null) {
            return details;
        }

        for (int i = 0; i < listSize; i++) {
            String element = listOps.index(queueKey, i);
            details.add(JSON.parseObject(element, ExecutionQueueDetail.class));
        }

        return details;
    }

    /**
     * 获取队列信息
     */
    public ExecutionQueue getQueue(String queueId) {
        String queue = redisTemplate.opsForValue().get(QUEUE_PREFIX + queueId);
        if (StringUtils.isNotBlank(queue)) {
            return JSON.parseObject(queue, ExecutionQueue.class);
        }
        return null;
    }

    public Long size(String queueId) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        String queueKey = QUEUE_DETAIL_PREFIX + queueId;
        return listOps.size(queueKey);
    }
}
