package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.enums.TestPlanExecuteQueueType;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.exception.MSException;
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

    public static final String QUEUE_PREFIX_TEST_PLAN = "test-plan-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_COLLECTION = "test-collection-execute:";

    public void executeTestPlan(TestPlan testPlan, String executionSource, String userId) {
        //todo 查询执行配置,配置下一步的队列
    }

    /**
     * 预执行执行测试计划
     */
    private void prepareExecuteTestPlan(TestPlan testPlan, String parentQueueId, String runMode, String executionSource, String userId) {
        if (testPlan == null || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException("test_plan.error");
        }
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());

            long pos = 0;
            List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
            for (TestPlan child : children) {
                childrenQueue.add(
                        new TestPlanExecutionQueue(child.getGroupId(), parentQueueId, child.getId(), pos++, runMode, executionSource, IDGenerator.nextStr(), userId, System.currentTimeMillis())
                );
            }

            if (StringUtils.equalsIgnoreCase(runMode, ApiBatchRunMode.SERIAL.name())) {
                //串行
                childrenQueue.forEach(childQueue -> {
                    redisTemplate.opsForList().rightPush(QUEUE_PREFIX_TEST_PLAN_GROUP + testPlan.getId(), JSON.toJSONString(childrenQueue));
                });
                executeNextTestPlanByGroupQueueId(testPlan.getId(), parentQueueId);
            } else {
                //并行
                childrenQueue.forEach(childQueue -> {
                    executeTestPlan(testPlanMapper.selectByPrimaryKey(childQueue.getTestPlanId()), childQueue.getExecutionSource(), childQueue.getCreateUser());
                });
            }
        } else {
            this.executeTestPlan(testPlan, executionSource, userId);
        }
    }

    /**
     * 单个执行测试计划
     */
    public void singleExecuteTestPlan(TestPlanExecuteRequest request, String userId) {
        prepareExecuteTestPlan(testPlanMapper.selectByPrimaryKey(request.getExecuteId()), null, request.getRunMode(), request.getExecutionSource(), userId);
    }

    /**
     * 批量执行测试计划
     */
    public void batchExecuteTestPlan(TestPlanBatchExecuteRequest request, String userId) {
        List<String> rightfulIds = testPlanService.selectRightfulIds(request.getExecuteIds());
        if (CollectionUtils.isNotEmpty(rightfulIds)) {

            String runMode = request.getRunMode();
            String queueId = IDGenerator.nextStr();
            long pos = 0;
            List<TestPlanExecutionQueue> testPlanExecutionQueues = new ArrayList<>();

            //遍历原始ID，只挑选符合条件的ID进行。防止顺序错乱。
            for (String testPlanId : request.getExecuteIds()) {
                if (rightfulIds.contains(testPlanId)) {
                    testPlanExecutionQueues.add(
                            new TestPlanExecutionQueue(queueId, null, testPlanId, pos++, runMode, request.getExecutionSource(), IDGenerator.nextStr(), userId, System.currentTimeMillis())
                    );
                }
            }
            if (StringUtils.equalsIgnoreCase(request.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                //串行
                testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                    redisTemplate.opsForList().rightPush(QUEUE_PREFIX_TEST_PLAN + queueId, JSON.toJSONString(testPlanExecutionQueue));
                });
                executeNextTestPlanByQueueId(queueId);
            } else {
                //并行
                testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                    prepareExecuteTestPlan(testPlanMapper.selectByPrimaryKey(testPlanExecutionQueue.getTestPlanId()),
                            null,
                            testPlanExecutionQueue.getRunMode(),
                            testPlanExecutionQueue.getExecutionSource(),
                            testPlanExecutionQueue.getCreateUser());
                });
            }
        }
    }

    //执行下一个测试计划组节点的测试计划
    private void executeNextTestPlanByGroupQueueId(String groupQueueId, String parentQueueId) {
        TestPlanExecutionQueue nextQueue = getNextDetail(groupQueueId, QUEUE_PREFIX_TEST_PLAN_GROUP, TestPlanExecutionQueue.class);
        if (nextQueue != null) {
            try {
                executeTestPlan(testPlanMapper.selectByPrimaryKey(nextQueue.getTestPlanId()), nextQueue.getExecutionSource(), nextQueue.getCreateUser());
            } catch (Exception e) {
                this.executeNextTestPlanByQueueId(groupQueueId);
            }
        } else {
            // todo 测试计划组执行完成

            if (StringUtils.isNotEmpty(parentQueueId)) {
                // 如果测试计划组是在批量执行时处理的，继续进行批量执行队列里的下个节点
                executeNextTestPlanByQueueId(parentQueueId);

            }
        }
    }

    //执行下一个批量执行节点的测试计划
    private void executeNextTestPlanByQueueId(String queueId) {
        TestPlanExecutionQueue nextQueue = getNextDetail(queueId, QUEUE_PREFIX_TEST_PLAN, TestPlanExecutionQueue.class);
        if (nextQueue != null) {
            try {
                prepareExecuteTestPlan(
                        testPlanMapper.selectByPrimaryKey(nextQueue.getTestPlanId()),
                        queueId,
                        nextQueue.getRunMode(),
                        nextQueue.getExecutionSource(),
                        nextQueue.getCreateUser());
            } catch (Exception e) {
                this.executeNextTestPlanByQueueId(queueId);
            }
        }
    }

    /**
     * 获取下一个队列节点
     */
    private <T> T getNextDetail(String queueId, String queueType, Class<T> formatClass) {
        String queueKey = this.genQueueKey(queueId, queueType);
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
                } catch (Exception ignore) {
                }
            }
        }

        if (StringUtils.isNotBlank(queueDetail)) {
            Long size = getQueueSize(queueId);
            if (size == null || size == 0) {
                // 最后一个节点清理队列
                deleteQueue(queueKey);
            }
            return JSON.parseObject(queueDetail, formatClass);
        }

        // 整体获取完，清理队列
        deleteQueue(queueKey);
        return null;
    }

    private void deleteQueue(String queueKey) {
        redisTemplate.delete(queueKey);
    }

    private Long getQueueSize(String queueKey) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.size(queueKey);
    }

    //生成队列key
    private String genQueueKey(String queueId, String queueType) {
        String key = "";
        if (StringUtils.equalsIgnoreCase(queueType, TestPlanExecuteQueueType.TEST_PLAN.name())) {
            key = QUEUE_PREFIX_TEST_PLAN;
        } else if (StringUtils.equalsIgnoreCase(queueType, TestPlanExecuteQueueType.TEST_PLAN_GROUP.name())) {
            key = QUEUE_PREFIX_TEST_PLAN_GROUP;
        } else if (StringUtils.equalsIgnoreCase(queueType, TestPlanExecuteQueueType.TEST_COLLECTION.name())) {
            key = QUEUE_PREFIX_TEST_COLLECTION;
        }
        return key + queueId;
    }
}
