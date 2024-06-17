package io.metersphere.plan.service;

import com.esotericsoftware.minlog.Log;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanReportPostParam;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanReportGenRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
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
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private PlanRunTestPlanApiCaseService planRunTestPlanApiCaseService;
    @Resource
    private PlanRunTestPlanApiScenarioService planRunTestPlanApiScenarioService;
    @Resource
    private TestPlanApiBatchRunBaseService testPlanApiBatchRunBaseService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public static final String QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE = "test-plan-batch-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_CASE_TYPE = "test-plan-case-type-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";

    public static final String LAST_QUEUE_PREFIX = "last-queue:";
    @Resource
    private TestPlanReportMapper testPlanReportMapper;

    // 停止测试计划的执行
    public void stopTestPlanRunning(String testPlanReportId) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportId);
        if (testPlanReport == null) {
            return;
        }
        if (testPlanReport.getIntegrated()) {
            TestPlanReportExample reportExample = new TestPlanReportExample();
            reportExample.createCriteria().andParentIdEqualTo(testPlanReportId);
            List<TestPlanReport> testPlanItemReport = testPlanReportMapper.selectByExample(reportExample);
            /*
                集成报告：要停止的是测试计划组执行
                这条测试计划所在队列，是以 test-plan-batch-execute:randomId 命名的
                要删除的队列：1. test-plan-group-execute:testPlanReportId（队列里放的是子测试计划的数据）
                            2. test-plan-case-type-execute:testPlanItemReportId（队列里放的是测试计划-用例类型的数据）
                            3. test-plan-collection-execute:testPlanItemReportId_testPlanParentCollectionId
                循环子报告进行报告结算
                进行报告结算
                继续执行    test-plan-batch-execute:randomId 队列的下一条
             */
            // 获取下一个要执行的测试计划节点，目的是得到最后一条的queueId
            TestPlanExecutionQueue nextTestPlanQueue = this.getNextQueue(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE);
            if (nextTestPlanQueue == null || !StringUtils.equalsIgnoreCase(nextTestPlanQueue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
                return;
            }

            String groupExecuteQueueId = genQueueKey(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE);
            this.deleteRedisKey(groupExecuteQueueId);
            testPlanItemReport.forEach(item -> {
                this.deepDeleteTestPlanCaseType(item);
                //统计子测试计划报告
                summaryTestPlanReport(item.getId(), false);
            });
            summaryTestPlanReport(testPlanReportId, true);
            this.testPlanExecuteQueueFinish(nextTestPlanQueue.getParentQueueId(), nextTestPlanQueue.getParentQueueType());
        } else {
            /*
                独立报告中，停止的是单独测试计划的执行
                这条测试计划是在批量执行队列中，还是在测试计划组执行队中， 通过要删除的队列1（因为当前节点在执行之前就被弹出）来确定。
                前者为 test-plan-group-execute:parentReportId  后者为 test-plan-batch-execute:randomId
                这条测试计划所在队列，是以 test-plan-group-execute:parentReportId 命名的
                要删除的队列：1. test-plan-case-type-execute:testPlanReportId（队列里放的是测试计划-用例类型的数据）
                            2. test-plan-collection-execute:testPlanReportId_testPlanParentCollectionId
                进行当前报告结算
                继续执行   队列的下一条
             */
            TestPlanExecutionQueue nextTestPlanQueue = this.getNextQueue(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_CASE_TYPE);
            if (nextTestPlanQueue == null || !StringUtils.equalsAnyIgnoreCase(nextTestPlanQueue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
                return;
            }
            this.deepDeleteTestPlanCaseType(testPlanReport);
            summaryTestPlanReport(testPlanReportId, false);
            this.testPlanExecuteQueueFinish(nextTestPlanQueue.getParentQueueId(), nextTestPlanQueue.getParentQueueType());
        }

        // todo @wxg 是在 deepDeleteTestPlanCaseType()方法中删除用例执行队列时，同步到执行机停止执行任务，还是其它操作， 由你来决定了
    }

    private void deepDeleteTestPlanCaseType(TestPlanReport report) {
        this.deleteRedisKey(genQueueKey(report.getId(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE));
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andTestPlanIdEqualTo(report.getTestPlanId()).andParentIdEqualTo(TestPlanConstants.DEFAULT_PARENT_ID);
        List<TestPlanCollection> parentTestPlanCollectionList = testPlanCollectionMapper.selectByExample(collectionExample);
        parentTestPlanCollectionList.forEach(parentCollection -> {

            this.deleteRedisKey(genQueueKey(report.getId() + "_" + parentCollection.getId(), QUEUE_PREFIX_TEST_PLAN_COLLECTION));

            //todo @Chen-Jianxing 这里要同步清理用例/场景的执行队列
        });
    }

    /**
     * 单个执行测试计划
     */
    public String singleExecuteTestPlan(TestPlanExecuteRequest request, String userId) {
        TestPlanExecutionQueue executionQueue = new TestPlanExecutionQueue();
        executionQueue.setSourceID(request.getExecuteId());
        executionQueue.setRunMode(request.getRunMode());
        executionQueue.setExecutionSource(request.getExecutionSource());
        executionQueue.setQueueId(IDGenerator.nextStr());
        executionQueue.setQueueType(QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        executionQueue.setCreateUser(userId);
        executionQueue.setPrepareReportId(IDGenerator.nextStr());

        TestPlanExecutionQueue singleExecuteRootQueue = new TestPlanExecutionQueue(
                0,
                userId,
                System.currentTimeMillis(),
                executionQueue.getQueueId(),
                QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE,
                null,
                null,
                request.getExecuteId(),
                request.getRunMode(),
                executionQueue.getExecutionSource(),
                IDGenerator.nextStr()
        );

        String redisKey = genQueueKey(executionQueue.getQueueId(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        redisTemplate.opsForList().rightPush(genQueueKey(executionQueue.getQueueId(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), JSON.toJSONString(singleExecuteRootQueue));
        redisTemplate.expire(genQueueKey(executionQueue.getQueueId(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), 1, TimeUnit.DAYS);

        LogUtils.info("测试计划（组）的单独执行start！计划报告[{}] , 资源ID[{}]", singleExecuteRootQueue.getPrepareReportId(), singleExecuteRootQueue.getSourceID());

        return executeTestPlanOrGroup(executionQueue);
    }

    private void setRedisForList(String key, List<String> list) {
        redisTemplate.opsForList().rightPushAll(key, list);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private void deleteRedisKey(String redisKey) {
        //清除list的key 和 last key节点
        redisTemplate.delete(redisKey);
        redisTemplate.delete(genQueueKey(redisKey, LAST_QUEUE_PREFIX));
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
                                    TaskTriggerMode.BATCH.name(),
                                    IDGenerator.nextStr()
                            )
                    );
                }
            }
            this.setRedisForList(genQueueKey(queueId, queueType), testPlanExecutionQueues.stream().map(JSON::toJSONString).toList());
            LogUtils.info("测试计划（组）的批量执行start！队列ID[{}] ,队列类型[{}] , 资源ID[{}]", queueId, queueType, JSON.toJSONString(rightfulIds));
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

        TestPlanReportGenRequest genReportRequest = new TestPlanReportGenRequest();
        genReportRequest.setTriggerMode(executionQueue.getExecutionSource());
        genReportRequest.setTestPlanId(executionQueue.getSourceID());
        genReportRequest.setProjectId(testPlan.getProjectId());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {

            testPlanService.setActualStartTime(executionQueue.getSourceID());
            testPlanService.setTestPlanUnderway(executionQueue.getSourceID());

            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());
            // 预生成计划组报告
            Map<String, String> reportMap = testPlanReportService.genReportByExecution(executionQueue.getPrepareReportId(), genReportRequest, executionQueue.getCreateUser());

            long pos = 0;
            List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
            String queueType = QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE;
            String queueId = executionQueue.getPrepareReportId();
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
                                reportMap.get(child.getId())
                        )
                );
            }

            LogUtils.info("计划组的执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}]", queueId, queueType, executionQueue.getParentQueueId(), executionQueue.getParentQueueType());

            if (CollectionUtils.isEmpty(childrenQueue)) {
                //本次的测试计划组执行完成
                this.testPlanGroupQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
            } else {
                this.setRedisForList(genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

                // 更新报告的执行时间
                extTestPlanReportMapper.batchUpdateExecuteTimeAndStatus(System.currentTimeMillis(), reportMap.values().stream().toList());

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
            }

            return executionQueue.getPrepareReportId();
        } else {
            Map<String, String> reportMap = testPlanReportService.genReportByExecution(executionQueue.getPrepareReportId(), genReportRequest, executionQueue.getCreateUser());
            executionQueue.setPrepareReportId(reportMap.get(executionQueue.getSourceID()));
            extTestPlanReportMapper.batchUpdateExecuteTimeAndStatus(System.currentTimeMillis(), reportMap.values().stream().toList());
            this.executeTestPlan(executionQueue);
            return executionQueue.getPrepareReportId();
        }
    }

    //执行测试计划里不同类型的用例  回调：caseTypeExecuteQueueFinish
    public void executeTestPlan(TestPlanExecutionQueue executionQueue) {
        testPlanService.setActualStartTime(executionQueue.getSourceID());
        testPlanService.setTestPlanUnderway(executionQueue.getSourceID());
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

        String queueId = executionQueue.getPrepareReportId();
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
                            executionQueue.getPrepareReportId())
            );
        }
        LogUtils.info("测试计划执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{}]", queueId, queueType, executionQueue.getParentQueueId(), executionQueue.getParentQueueType(), runMode);
        if (CollectionUtils.isEmpty(childrenQueue)) {
            //本次的测试计划组执行完成
            this.testPlanExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
        } else {
            this.setRedisForList(genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

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
    }

    //执行测试集 -- 回调：collectionExecuteQueueFinish
    private void executeByTestPlanCollection(TestPlanExecutionQueue executionQueue) {
        TestPlanCollection parentCollection = testPlanCollectionMapper.selectByPrimaryKey(executionQueue.getSourceID());
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andParentIdEqualTo(executionQueue.getSourceID());
        List<TestPlanCollection> childrenList = testPlanCollectionMapper.selectByExample(example);

        int pos = 0;
        List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();

        String queueId = executionQueue.getPrepareReportId() + "_" + parentCollection.getId();
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
                            executionQueue.getPrepareReportId()) {{
                        this.setTestPlanCollectionJson(JSON.toJSONString(collection));
                    }}
            );
        }
        LogUtils.info("测试计划不同用例类型的执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{}]", queueId, queueType, executionQueue.getParentQueueId(), executionQueue.getParentQueueType(), executionQueue.getRunMode());
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

    /**
     * 批量执行单个测试集的用例
     *
     * @param testPlanExecutionQueue
     */
    private void executeCase(TestPlanExecutionQueue testPlanExecutionQueue) {
        String queueId = testPlanExecutionQueue.getQueueId();
        LogUtils.info("测试集执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{}]", queueId, testPlanExecutionQueue.getQueueType(), testPlanExecutionQueue.getParentQueueId(), testPlanExecutionQueue.getParentQueueType(), testPlanExecutionQueue.getRunMode());
        try {
            boolean isFinish = false;
            TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
            TestPlanCollection extendedRootCollection = testPlanApiBatchRunBaseService.getExtendedRootCollection(collection);
            String executeMethod = extendedRootCollection == null ? collection.getExecuteMethod() : extendedRootCollection.getExecuteMethod();
            if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.API_CASE.getKey())) {
                if (StringUtils.equals(executeMethod, ApiBatchRunMode.PARALLEL.name())) {
                    isFinish = planRunTestPlanApiCaseService.parallelExecute(testPlanExecutionQueue);
                } else {
                    isFinish = planRunTestPlanApiCaseService.serialExecute(testPlanExecutionQueue);
                }
            } else if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.SCENARIO_CASE.getKey())) {
                if (StringUtils.equals(executeMethod, ApiBatchRunMode.PARALLEL.name())) {
                    isFinish = planRunTestPlanApiScenarioService.parallelExecute(testPlanExecutionQueue);
                } else {
                    isFinish = planRunTestPlanApiScenarioService.serialExecute(testPlanExecutionQueue);
                }
            }
            if (isFinish) {
                // 如果没有要执行的用例（可能会出现空测试集的情况），直接调用回调
                collectionExecuteQueueFinish(queueId);
            }
        } catch (Exception e) {
            Log.error("按测试集执行失败!", e);
            collectionExecuteQueueFinish(queueId);
        }
    }

    //测试集执行完成
    public void collectionExecuteQueueFinish(String queueID) {
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
        LogUtils.info("收到测试集执行完成的信息： 队列ID[{}],队列类型[{}]，下一个节点的执行工作准备中...", queueID, queueType);
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                try {
                    LogUtils.info("测试集该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.collectionExecuteQueueFinish(nextQueue.getQueueId());
                }
            } else {
                //当前测试集执行完毕
                LogUtils.info("测试集串行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            LogUtils.info("测试集并行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划中当前用例类型的全部执行完成
    private void caseTypeExecuteQueueFinish(String queueID, String queueType) {
        LogUtils.info("收到用例类型执行队列的执行完成的信息： 队列ID[{}],队列类型[{}]，下一个节点的执行工作准备中...", queueID, queueType);
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                try {
                    LogUtils.info("用例类型该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.caseTypeExecuteQueueFinish(nextQueue.getQueueId(), nextQueue.getQueueType());
                }
            } else {
                //当前测试计划执行完毕
                LogUtils.info("用例类型执行队列串行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            LogUtils.info("用例类型执行队列并行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划执行完成
    private void testPlanExecuteQueueFinish(String queueID, String queueType) {
        LogUtils.info("收到测试计划执行完成的信息： 队列ID[{}],队列类型[{}]，下一个节点的执行工作准备中...", queueID, queueType);
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                try {
                    LogUtils.info("测试计划该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.testPlanExecuteQueueFinish(nextQueue.getQueueId(), nextQueue.getQueueType());
                }
            } else {
                LogUtils.info("测试计划串行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
                this.queueExecuteFinish(nextQueue);
            }
        } else if (nextQueue.isLastOne()) {
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            LogUtils.info("测试计划并行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
            this.queueExecuteFinish(nextQueue);
        }
    }

    //测试计划批量执行队列节点执行完成
    private void testPlanGroupQueueFinish(String queueID, String queueType) {
        LogUtils.info("收到计划组执行完成的信息： 队列ID[{}],队列类型[{}]，下一个节点的执行工作准备中...", queueID, queueType);
        TestPlanExecutionQueue nextQueue = getNextQueue(queueID, queueType);
        if (nextQueue == null) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                try {
                    LogUtils.info("计划组该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue);
                } catch (Exception e) {
                    this.testPlanGroupQueueFinish(queueID, queueType);
                }
            } else {
                LogUtils.info("计划组并行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
            }
        } else {
            LogUtils.info("计划组并行执行Finish！ --- 队列ID[{}],队列类型[{}]，已经执行完毕！", queueID, queueType);
            //并行时，调用回调时意味着执行结束，所以判断是否是当前队列最后一个从而结束队列
            this.queueExecuteFinish(nextQueue);
        }

    }

    private void executeNextNode(TestPlanExecutionQueue queue) {
        LogUtils.info("开始执行下一个节点： --- 队列ID[{}],队列类型[{}]，预生成报告ID[{}]", queue.getQueueId(), queue.getQueueType(), queue.getPrepareReportId());
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

    private void summaryTestPlanReport(String reportId, boolean isGroupReport) {
        LogUtils.info("开始合并报告： --- 报告ID[{}],是否是报告组[{}]", reportId, isGroupReport);
        try {
            if (isGroupReport) {
                testPlanReportService.summaryGroupReport(reportId);
            } else {
                testPlanReportService.summaryPlanReport(reportId);
            }

            TestPlanReportPostParam postParam = new TestPlanReportPostParam();
            postParam.setReportId(reportId);
            // 执行生成报告, 执行状态为已完成, 执行及结束时间为当前时间
            postParam.setEndTime(System.currentTimeMillis());
            postParam.setExecStatus(ExecStatus.COMPLETED.name());
            testPlanReportService.postHandleReport(postParam);

            if (!isGroupReport) {
                TestPlanReport testPlanReport = testPlanReportService.selectById(reportId);
                if (testPlanReport != null) {
                    testPlanService.refreshTestPlanStatus(testPlanReport.getTestPlanId());
                }
            }
        } catch (Exception e) {
            LogUtils.error("Cannot find test plan report for " + reportId, e);
        }
    }

    private void queueExecuteFinish(TestPlanExecutionQueue queue) {
        LogUtils.info("当前节点执行完成： --- 队列ID[{}],队列类型[{}]，父队列ID[{}]，父队列类型[{}]", queue.getQueueId(), queue.getQueueType(), queue.getParentQueueId(), queue.getParentQueueType());
        if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
            if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
                // 计划组报告汇总并统计
                this.summaryTestPlanReport(queue.getQueueId(), true);
            } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
                /*
                    此时处于批量勾选执行中的游离态测试计划执行。所以队列顺序为：QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE -> QUEUE_PREFIX_TEST_PLAN_CASE_TYPE。
                    此时queue节点为testPlanCollection的节点。  而测试计划节点（串行状态下）在执行之前就被弹出了。
                    所以获取报告ID的方式为读取queueId （caseType队列和collection队列的queueId都是报告ID）
                 */
                this.summaryTestPlanReport(queue.getQueueId(), false);
            }
            this.testPlanGroupQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        } else if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
            // 计划报告汇总并统计
            this.summaryTestPlanReport(queue.getQueueId(), false);
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
            // 重试1次获取
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
            queueDetail = redisTemplate.opsForList().leftPop(queueKey);
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
