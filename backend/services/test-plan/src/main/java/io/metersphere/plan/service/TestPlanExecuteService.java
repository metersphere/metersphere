package io.metersphere.plan.service;

import com.esotericsoftware.minlog.Log;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollection;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.dto.TestPlanReportPostParam;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanReportGenRequest;
import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.ExecStatus;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanExecuteService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public static final String QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE = "test-plan-batch-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_CASE_TYPE = "test-plan-case-type-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";

    public static final String LAST_QUEUE_PREFIX = "last-queue:";
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

    private void setRedisForList(String key, List<String> list) {
        redisTemplate.opsForList().rightPushAll(key, list);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }
    //批量执行测试计划组
    public void batchExecuteTestPlan(TestPlanBatchExecuteRequest request, String userId) {
        List<String> rightfulIds = testPlanService.selectRightfulIds(request.getExecuteIds());
        if (CollectionUtils.isNotEmpty(rightfulIds)) {
            String runMode = request.getRunMode();
            String queueId = IDGenerator.nextStr();
            String queueType = QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE;
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
                                    IDGenerator.nextStr()
                            )
                    );
                }
            }
            this.setRedisForList(genQueueKey(queueId, queueType), testPlanExecutionQueues.stream().map(JSON::toJSONString).toList());

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
            // 预生成计划组报告
            TestPlanReportGenRequest genReportRequest = new TestPlanReportGenRequest();
            genReportRequest.setTriggerMode(executionQueue.getExecutionSource());
            genReportRequest.setTestPlanId(executionQueue.getSourceID());
            genReportRequest.setProjectId(testPlan.getProjectId());
            Map<String, String> reportMap = testPlanReportService.genReportByExecution(genReportRequest, executionQueue.getCreateUser());
            executionQueue.setPrepareReportId(reportMap.get(executionQueue.getSourceID()));

            long pos = 0;
            List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
            String queueType = QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE;
            for (TestPlan child : children) {
                childrenQueue.add(
                        new TestPlanExecutionQueue(
                                pos++,
                                executionQueue.getCreateUser(),
                                System.currentTimeMillis(),
                                reportMap.get(child.getId()),
                                queueType,
                                executionQueue.getQueueId(),
                                executionQueue.getQueueType(),
                                child.getId(),
                                executionQueue.getRunMode(),
                                executionQueue.getExecutionSource(),
                                reportMap.get(child.getId())
                        )
                );
            }
            if (CollectionUtils.isEmpty(childrenQueue)) {
                //本次的测试计划组执行完成
                this.testPlanGroupQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
            } else {
                this.setRedisForList(genQueueKey(executionQueue.getQueueId(), queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

                // 更新报告的执行时间
                extTestPlanReportMapper.batchUpdateExecuteTime(reportMap.values().stream().toList());

                if (StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行
                    TestPlanExecutionQueue nextQueue = this.getNextQueue(executionQueue.getQueueId(), queueType);
                    executeTestPlan(nextQueue);
                } else {
                    //并行
                    childrenQueue.forEach(childQueue -> {
                        executeTestPlan(childQueue);
                    });
                }
            }

            return executionQueue.getPrepareReportId();
        } else {
            return this.executeTestPlan(executionQueue);
        }
    }

    //执行测试计划里不同类型的用例  回调：caseTypeExecuteQueueFinish
    public String executeTestPlan(TestPlanExecutionQueue executionQueue) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(executionQueue.getSourceID());
        TestPlanCollectionExample testPlanCollectionExample = new TestPlanCollectionExample();
        testPlanCollectionExample.createCriteria().andTestPlanIdEqualTo(testPlan.getId()).andParentIdEqualTo("NONE");
        testPlanCollectionExample.setOrderByClause("pos asc");
        //过滤掉功能用例的测试集
        List<TestPlanCollection> testPlanCollectionList = testPlanCollectionMapper.selectByExample(testPlanCollectionExample).stream().filter(
                testPlanCollection -> !StringUtils.equalsIgnoreCase(testPlanCollection.getType(), CaseType.FUNCTIONAL_CASE.getKey())
        ).toList();

        int pos = 0;
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        String runMode = StringUtils.isBlank(testPlanConfig.getCaseRunMode()) ? ApiBatchRunMode.SERIAL.name() : testPlanConfig.getCaseRunMode();

        String queueId = IDGenerator.nextStr();
        String queueType = QUEUE_PREFIX_TEST_PLAN_CASE_TYPE;
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
                            runMode,
                            executionQueue.getExecutionSource(),
                            IDGenerator.nextStr())
            );
        }

        if (CollectionUtils.isEmpty(childrenQueue)) {
            //本次的测试计划组执行完成
            this.testPlanExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
        } else {
            this.setRedisForList(genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

            // 预生成计划报告
            if (StringUtils.isBlank(executionQueue.getPrepareReportId())) {
                // 暂时未直接生成或通过组间接生成计划报告
                TestPlanReportGenRequest genReportRequest = new TestPlanReportGenRequest();
                genReportRequest.setTriggerMode(executionQueue.getExecutionSource());
                genReportRequest.setTestPlanId(executionQueue.getSourceID());
                genReportRequest.setProjectId(testPlan.getProjectId());
                Map<String, String> reportMap = testPlanReportService.genReportByExecution(genReportRequest, executionQueue.getCreateUser());
                executionQueue.setPrepareReportId(reportMap.get(executionQueue.getSourceID()));
                extTestPlanReportMapper.batchUpdateExecuteTime(reportMap.values().stream().toList());
            }

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
        }

        return executionQueue.getPrepareReportId();
    }

    //执行测试集 -- 回调：collectionExecuteQueueFinish
    private void executeByTestPlanCollection(TestPlanExecutionQueue executionQueue) {
        TestPlanCollection parentCollection = testPlanCollectionMapper.selectByPrimaryKey(executionQueue.getSourceID());
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andParentIdEqualTo(executionQueue.getSourceID());
        List<TestPlanCollection> childrenList = testPlanCollectionMapper.selectByExample(example);

        int pos = 0;
        List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();

        String queueId = IDGenerator.nextStr();
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
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
                            collection.getExecuteMethod(),
                            executionQueue.getExecutionSource(),
                            IDGenerator.nextStr()) {{
                        this.setTestPlanCollectionJson(JSON.toJSONString(collection));
                    }}
            );
        }
        if (CollectionUtils.isEmpty(childrenQueue)) {
            //本次的测试集执行完成
            this.caseTypeExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
        } else {
            this.setRedisForList(genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());
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

    }

    // todo  @Chen jianxing 执行用例
    private void executeCase(TestPlanExecutionQueue testPlanExecutionQueue) {
        String queueId = testPlanExecutionQueue.getQueueId();
        String queueType = testPlanExecutionQueue.getQueueType();

        try {
            TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
            String runMode = collection.getExecuteMethod();
            String environmentId = collection.getEnvironmentId();
            if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.API_CASE.getKey())) {
                // todo 执行API用例    预生成报告时会将要执行的用例记录在test_plan_report_api_case表中，通过它查询
            } else if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.SCENARIO_CASE.getKey())) {
                // todo 执行场景用例  预生成报告时会将要执行的用例记录在test_plan_report_api_scenario表中，通过它查询
            }
            //执行完成之后需要回调：  collectionExecuteQueueFinish
            //如果没有要执行的用例（可能会出现空测试集的情况），直接调用collectionExecuteQueueFinish(queueId)
        } catch (Exception e) {
            Log.error("按测试集执行失败!", e);
            collectionExecuteQueueFinish(queueId);
        }
    }


    //测试集执行完成
    public void collectionExecuteQueueFinish(String queueID) {
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                try {
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.collectionExecuteQueueFinish(nextQueue.getQueueId());
                }
            } else {
                //当前测试集执行完毕
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划中当前用例类型的全部执行完成
    private void caseTypeExecuteQueueFinish(String queueID, String queueType) {
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                try {
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.caseTypeExecuteQueueFinish(nextQueue.getQueueId(), nextQueue.getQueueType());
                }
            } else {
                //当前测试计划执行完毕
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划执行完成
    private void testPlanExecuteQueueFinish(String queueID, String queueType) {
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                try {
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.testPlanExecuteQueueFinish(nextQueue.getQueueId(), nextQueue.getQueueType());
                }
            } else {
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划批量执行队列节点执行完成
    private void testPlanGroupQueueFinish(String queueID, String queueType) {
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (nextQueue == null) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                try {
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.testPlanGroupQueueFinish(queueID, queueType);
                }
            }
        } else {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }

    }

    private void executeNextNode(TestPlanExecutionQueue queue) {
        if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
            this.executeTestPlanOrGroup(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
            this.executeTestPlan(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
            this.executeByTestPlanCollection(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_COLLECTION)) {
            this.executeCase(queue);
        }
    }

    private void queueExecuteFinish(TestPlanExecutionQueue queue) {
        if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
            // 计划组报告汇总并统计
            testPlanReportService.summaryGroupReport(queue.getPrepareReportId());
            TestPlanReportPostParam postParam = new TestPlanReportPostParam();
            postParam.setReportId(queue.getPrepareReportId());
            // 执行生成报告, 执行状态为已完成, 执行及结束时间为当前时间
            postParam.setEndTime(System.currentTimeMillis());
            postParam.setExecStatus(ExecStatus.COMPLETED.name());
            testPlanReportService.postHandleReport(postParam);

            this.testPlanGroupQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        } else if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
            // 计划报告汇总并统计
            testPlanReportService.summaryPlanReport(queue.getPrepareReportId());
            TestPlanReportPostParam postParam = new TestPlanReportPostParam();
            postParam.setReportId(queue.getPrepareReportId());
            // 执行生成报告, 执行状态为已完成, 执行及结束时间为当前时间
            postParam.setEndTime(System.currentTimeMillis());
            postParam.setExecStatus(ExecStatus.COMPLETED.name());
            testPlanReportService.postHandleReport(postParam);

            this.testPlanExecuteQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        } else if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
            this.caseTypeExecuteQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        }
    }

    /**
     * 获取下一个队列节点
     */
    private TestPlanExecutionQueue getNextQueue(String queueId, String queueType) {
        if (StringUtils.isAnyBlank(queueId, queueType)) {
            return null;
        }

        String queueKey = this.genQueueKey(queueId, queueType);
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        String queueDetail = listOps.leftPop(queueKey);
        if (StringUtils.isBlank(queueDetail)) {
            // 重试2次获取
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
            Long size = listOps.size(queueKey);
            if (size == null || size == 0) {
                returnQueue.setLastOne(true);
                if (StringUtils.equalsIgnoreCase(returnQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行的执行方式意味着最后一个节点要单独存储
                    redisTemplate.opsForValue().setIfAbsent(genQueueKey(queueKey, LAST_QUEUE_PREFIX), JSON.toJSONString(returnQueue), 1, TimeUnit.DAYS);
                }
                // 最后一个节点清理队列
                deleteQueue(queueKey);
            }
            return returnQueue;
        } else {
            String lastQueueJson = redisTemplate.opsForValue().getAndDelete(genQueueKey(queueKey, LAST_QUEUE_PREFIX));
            if (StringUtils.isNotBlank(lastQueueJson)) {
                TestPlanExecutionQueue nextQueue = JSON.parseObject(lastQueueJson, TestPlanExecutionQueue.class);
                nextQueue.setExecuteFinish(true);
                return nextQueue;
            }
        }

        // 整体获取完，清理队列
        deleteQueue(queueKey);
        return null;
    }


    private void deleteQueue(String queueKey) {
        redisTemplate.delete(queueKey);
    }

    //生成队列key
    private String genQueueKey(String queueId, String queueType) {
        return queueType + queueId;
    }
}
