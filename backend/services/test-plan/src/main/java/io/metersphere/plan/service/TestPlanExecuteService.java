package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CaseType;
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
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public static final String QUEUE_PREFIX_TEST_PLAN_BATCH = "test-plan-batch-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";
    public static final String QUEUE_PREFIX_TEST_CASE = "test-plan-collection-execute:";

    //单独执行测试计划
    /**
     * 单个执行测试计划
     */
    public String singleExecuteTestPlan(TestPlanExecuteRequest request, String userId) {
        TestPlanExecutionQueue executionQueue = new TestPlanExecutionQueue();
        executionQueue.setSourceID(request.getExecuteId());
        executionQueue.setRunMode(request.getRunMode());
        executionQueue.setExecutionSource(request.getExecutionSource());
        executionQueue.setCreateUser(userId);
        executionQueue.setPrepareReportId(IDGenerator.nextStr());
        return executeTestPlanOrGroup(executionQueue);
    }

    //批量执行测试计划
    public void batchExecuteTestPlan(TestPlanBatchExecuteRequest request, String userId) {
        List<String> rightfulIds = testPlanService.selectRightfulIds(request.getExecuteIds());
        if (CollectionUtils.isNotEmpty(rightfulIds)) {
            String runMode = request.getRunMode();
            String queueId = IDGenerator.nextStr();
            String queueType = QUEUE_PREFIX_TEST_PLAN_BATCH;
            long pos = 0;
            List<TestPlanExecutionQueue> testPlanExecutionQueues = new ArrayList<>();

            //遍历原始ID，只挑选符合条件的ID进行。防止顺序错乱。
            for (String testPlanId : request.getExecuteIds()) {
                if (rightfulIds.contains(testPlanId)) {
                    testPlanExecutionQueues.add(
                            new TestPlanExecutionQueue(
                                    pos++,
                                    userId,
                                    System.currentTimeMillis(),
                                    queueId,
                                    queueType,
                                    null,
                                    null,
                                    testPlanId,
                                    runMode,
                                    request.getExecutionSource(),
                                    IDGenerator.nextStr(),
                                    false
                            )
                    );
                }
            }
            testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                redisTemplate.opsForList().rightPush(testPlanExecutionQueue.getQueueType() + testPlanExecutionQueue.getQueueId(), JSON.toJSONString(testPlanExecutionQueue));
            });
            if (StringUtils.equalsIgnoreCase(request.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                //串行
                TestPlanExecutionQueue nextQueue = this.getNextQueue(queueId, queueType);
                executeTestPlanOrGroup(nextQueue);
            } else {
                //并行
                testPlanExecutionQueues.forEach(testPlanExecutionQueue -> {
                    executeTestPlanOrGroup(testPlanExecutionQueue);
                });
            }
        }
    }

    //执行测试计划组
    private String executeTestPlanOrGroup(TestPlanExecutionQueue executionQueue) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(executionQueue.getSourceID());
        if (testPlan == null || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException("test_plan.error");
        }
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());
            long pos = 0;
            List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
            String queueId = IDGenerator.nextStr();
            String queueType = QUEUE_PREFIX_TEST_PLAN_GROUP;
            for (TestPlan child : children) {
                childrenQueue.add(
                        new TestPlanExecutionQueue(
                                pos++,
                                executionQueue.getCreateUser(),
                                System.currentTimeMillis(),
                                queueId,
                                queueType,
                                executionQueue.getQueueId(),
                                executionQueue.getQueueType(),
                                child.getId(),
                                executionQueue.getRunMode(),
                                executionQueue.getExecutionSource(),
                                IDGenerator.nextStr(),
                                false
                        )
                );
            }
            childrenQueue.forEach(childQueue -> {
                redisTemplate.opsForList().rightPush(childQueue.getQueueType() + childQueue.getQueueId(), JSON.toJSONString(childQueue));
            });
            if (StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                //串行
                TestPlanExecutionQueue nextQueue = this.getNextQueue(queueId, queueType);
                executeTestPlan(nextQueue);
            } else {
                //并行
                childrenQueue.forEach(childQueue -> {
                    executeTestPlan(childQueue);
                });
            }
            return executionQueue.getPrepareReportId();
        } else {
            return this.executeTestPlan(executionQueue);
        }
    }

    //执行测试计划
    public String executeTestPlan(TestPlanExecutionQueue executionQueue) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(executionQueue.getSourceID());
        TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo(testPlan.getId());
        testPlanCollectionExample.setOrderByClause("pos asc");
        //过滤掉功能用例的测试集
        List<TestPlanCollection> testPlanCollectionList = testPlanCollectionMapper.selectByExample(testPlanCollectionExample).stream().filter(
                testPlanCollection -> !StringUtils.equalsIgnoreCase(testPlanCollection.getType(), CaseType.FUNCTIONAL_CASE.getKey())
        ).toList();

        int pos = 0;
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        String runMode = StringUtils.isBlank(testPlanConfig.getCaseRunMode()) ? ApiBatchRunMode.SERIAL.name() : testPlanConfig.getCaseRunMode();

        String queueId = IDGenerator.nextStr();
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
        List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
        for (TestPlanCollection collection : testPlanCollectionList) {
            childrenQueue.add(
                    new TestPlanExecutionQueue(
                            pos++,
                            executionQueue.getCreateUser(),
                            System.currentTimeMillis(),
                            queueId,
                            queueType,
                            executionQueue.getQueueId(),
                            executionQueue.getQueueType(),
                            collection.getId(),
                            executionQueue.getRunMode(),
                            executionQueue.getExecutionSource(),
                            IDGenerator.nextStr(),
                            false)
            );
        }
        childrenQueue.forEach(childQueue -> {
            redisTemplate.opsForList().rightPush(childQueue.getQueueType() + childQueue.getQueueId(), JSON.toJSONString(childQueue));
        });

        // todo Song-cc  这里是否要生成测试计划报告，并且记录测试计划里用例的执行信息？

        //开始根据测试计划集合执行测试用例
        if (StringUtils.equalsIgnoreCase(runMode, ApiBatchRunMode.SERIAL.name())) {
            //串行
            TestPlanExecutionQueue nextQueue = this.getNextQueue(queueId, queueType);
            this.executeByTestPlanCollection(nextQueue);
        } else {
            //并行
            childrenQueue.forEach(childQueue -> {
                this.executeByTestPlanCollection(childQueue);
            });
        }
        return executionQueue.getPrepareReportId();
    }

    //执行测试集
    private void executeByTestPlanCollection(TestPlanExecutionQueue executionQueue) {
        TestPlanCollection parentCollection = testPlanCollectionMapper.selectByPrimaryKey(executionQueue.getParentQueueId());
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andParentIdEqualTo(executionQueue.getSourceID());
        List<TestPlanCollection> childrenList = testPlanCollectionMapper.selectByExample(example);

        int pos = 0;
        List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();

        String queueId = IDGenerator.nextStr();
        String queueType = QUEUE_PREFIX_TEST_CASE;
        for (TestPlanCollection collection : childrenList) {
            childrenQueue.add(
                    new TestPlanExecutionQueue(
                            pos++,
                            executionQueue.getCreateUser(),
                            System.currentTimeMillis(),
                            queueId,
                            queueType,
                            executionQueue.getQueueId(),
                            executionQueue.getQueueType(),
                            collection.getId(),
                            executionQueue.getRunMode(),
                            executionQueue.getExecutionSource(),
                            IDGenerator.nextStr(),
                            false)
            );
        }
        childrenQueue.forEach(childQueue -> {
            redisTemplate.opsForList().rightPush(childQueue.getQueueType() + childQueue.getQueueId(), JSON.toJSONString(childQueue));
        });
        if (StringUtils.equalsIgnoreCase(parentCollection.getExecuteMethod(), ApiBatchRunMode.SERIAL.name())) {
            //串行
            TestPlanExecutionQueue nextQueue = this.getNextQueue(queueId, queueType);
            this.executeCase(nextQueue);
        } else {
            //并行
            childrenQueue.forEach(childQueue -> {
                this.executeCase(childQueue);
            });
        }
    }

    // todo  @Chen jianxing 执行用例
    private void executeCase(TestPlanExecutionQueue testPlanExecutionQueue) {

    }


    //todo 测试用例执行完成中的某一条
    private void TestCaseExecuteQueueFinish(String queueID, String queueType) {
        /**
         * 当队列全部执行完时，nextQueue将不会再查到。
         * 此时如何调用父类队列的执行方式？
         */
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (nextQueue != null) {
            try {
                executeCase(nextQueue);
            } catch (Exception e) {
                this.testPlanCollectionExecuteQueueFinish(nextQueue.getParentQueueId(), nextQueue.getParentQueueType());
            }
        } else {
            //            testPlanCollectionExecuteQueueFinish(queueID, queueType);
        }
    }

    //todo 测试集执行完成中的某一条
    private void testPlanCollectionExecuteQueueFinish(String queueID, String queueType) {

    }

    //todo 测试计划执行完成中的某一条
    private void TestPlanGroupExecuteQueueFinish(String queueID, String queueType) {
    }

    //测试计划批量执行队列节点执行完成
    private void batchTestPlanExecuteQueueFinish(String queueId, String queueType) {
        TestPlanExecutionQueue nextQueue = getNextQueue(queueId, queueType);
        if (nextQueue != null) {
            try {
                executeTestPlanOrGroup(nextQueue);
            } catch (Exception e) {
                this.batchTestPlanExecuteQueueFinish(queueId, queueType);
            }
        }
    }


    /**
     * 获取下一个队列节点
     */
    private TestPlanExecutionQueue getNextQueue(String queueId, String queueType) {
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
            TestPlanExecutionQueue returnQueue = JSON.parseObject(queueDetail, TestPlanExecutionQueue.class);
            Long size = getQueueSize(queueId);
            if (size == null || size == 0) {
                returnQueue.setLastNode(true);
                // 最后一个节点清理队列
                deleteQueue(queueKey);
            }
            return returnQueue;
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
        return queueType + queueId;
    }
}
