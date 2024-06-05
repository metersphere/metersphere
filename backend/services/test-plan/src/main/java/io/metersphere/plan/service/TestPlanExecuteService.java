package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanExecuteService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanService testPlanService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public static final String TEST_PLAN_QUEUE_PREFIX = "queue:test-plan:";

    private TestPlanExecutionQueue genQueue(String testPlanId, String queueId, long pos, String userId, String executeMode) {
        TestPlanExecutionQueue testPlanExecutionQueue = new TestPlanExecutionQueue();
        testPlanExecutionQueue.setTestPlanId(testPlanId);
        testPlanExecutionQueue.setQueueId(queueId);
        testPlanExecutionQueue.setPos(pos);
        testPlanExecutionQueue.setPrepareReportId(IDGenerator.nextStr());
        testPlanExecutionQueue.setCreateUser(userId);
        testPlanExecutionQueue.setCreateTime(System.currentTimeMillis());
        testPlanExecutionQueue.setRunMode(executeMode);
        return testPlanExecutionQueue;
    }

    public void execute(TestPlanExecuteRequest request, String userId) {

        List<String> rightfulIds = testPlanService.selectRightfulIds(request.getExecuteIds());

        if (CollectionUtils.isNotEmpty(rightfulIds)) {
            //遍历原始ID，只挑选符合条件的ID进行。防止顺序错乱。
            String executeMode = request.getExecuteMode();
            String queueId = IDGenerator.nextStr();
            long pos = 1;
            List<TestPlanExecutionQueue> testPlanExecutionQueues = new ArrayList<>();
            for (String testPlanId : request.getExecuteIds()) {
                List<TestPlan> childList = testPlanService.selectChildPlanByGroupId(testPlanId);
                if (CollectionUtils.isNotEmpty(childList)) {
                    for (TestPlan child : childList) {
                        testPlanExecutionQueues.add(genQueue(child.getId(), queueId, pos++, userId, executeMode));
                    }
                } else {
                    testPlanExecutionQueues.add(genQueue(testPlanId, queueId, pos++, userId, executeMode));
                }
            }
            if (StringUtils.equalsIgnoreCase(request.getExecuteMode(), ApiBatchRunMode.SERIAL.name())) {
                //串行
                testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                    redisTemplate.opsForList().rightPush(TEST_PLAN_QUEUE_PREFIX + queueId, JSON.toJSONString(testPlanExecutionQueue));
                });
                try {
                    executeByExecutionQueue(getNextDetail(queueId));
                } catch (Exception ignore) {
                }
            } else {
                //并行
                testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                    executeByExecutionQueue(testPlanExecutionQueue);
                });
            }
        }
    }

    public void executeByExecutionQueue(TestPlanExecutionQueue queue) {
        Thread.startVirtualThread(() -> {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(queue.getTestPlanId());
            // todo 获取测试规划，通过测试规划执行方式确定用例的执行方式
        });
    }

    /**
     * 获取下一个节点
     */
    public TestPlanExecutionQueue getNextDetail(String queueId) throws Exception {
        String queueKey = TEST_PLAN_QUEUE_PREFIX + queueId;
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        String queueDetail = listOps.leftPop(queueKey);
        if (StringUtils.isBlank(queueDetail)) {
            // 重试3次获取
            for (int i = 0; i < 3; i++) {
                queueDetail = redisTemplate.opsForList().leftPop(queueKey);
                if (StringUtils.isNotBlank(queueDetail)) {
                    break;
                }
                Thread.sleep(1000);
            }
        }

        if (StringUtils.isNotBlank(queueDetail)) {
            Long size = size(queueId);
            if (size == null || size == 0) {
                // 最后一个节点清理队列
                deleteQueue(queueId);
            }
            return JSON.parseObject(queueDetail, TestPlanExecutionQueue.class);
        }

        // 整体获取完，清理队列
        deleteQueue(queueId);

        return null;
    }

    public void deleteQueue(String queueId) {
        redisTemplate.delete(TEST_PLAN_QUEUE_PREFIX + queueId);
    }

    public Long size(String queueId) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        String queueKey = TEST_PLAN_QUEUE_PREFIX + queueId;
        return listOps.size(queueKey);
    }
}
