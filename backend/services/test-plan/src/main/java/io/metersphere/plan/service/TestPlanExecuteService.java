package io.metersphere.plan.service;

import com.esotericsoftware.minlog.Log;
import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.api.service.ApiBatchRunBaseService;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.functional.constants.AssociateCaseType;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanBatchExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanExecuteRequest;
import io.metersphere.plan.dto.request.TestPlanReportGenRequest;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.service.BaseTaskHubService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.metersphere.plan.service.TestPlanExecuteSupportService.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanExecuteService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanCollectionMapper extTestPlanCollectionMapper;
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
    private ExtTestPlanReportApiCaseMapper extTestPlanReportApiCaseMapper;
    @Resource
    private ExtTestPlanReportApiScenarioMapper extTestPlanReportApiScenarioMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private TestPlanExecuteSupportService testPlanExecuteSupportService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private BaseTaskHubService baseTaskHubService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ApiBatchRunBaseService apiBatchRunBaseService;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;

    // 停止测试计划的执行
    public void stopTestPlanRunning(String testPlanReportId) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportId);
        if (testPlanReport == null
                || StringUtils.equalsAnyIgnoreCase(testPlanReport.getExecStatus(), ExecStatus.COMPLETED.name(), ExecStatus.STOPPED.name())) {
            // 已经执行完成或者已经停止的测试计划，不再操作
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
            TestPlanExecutionQueue nextTestPlanQueue = testPlanExecuteSupportService.getNextQueue(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE);
            if (nextTestPlanQueue == null || !StringUtils.equalsIgnoreCase(nextTestPlanQueue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
                return;
            }

            String groupExecuteQueueId = testPlanExecuteSupportService.genQueueKey(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE);
            testPlanExecuteSupportService.deleteRedisKey(groupExecuteQueueId);
            testPlanItemReport.forEach(item -> {
                //处理未完成的子报告
                if (!StringUtils.equalsIgnoreCase(item.getExecStatus(), ExecStatus.COMPLETED.name())) {
                    this.deepDeleteTestPlanCaseType(item);
                    //统计子测试计划报告
                    testPlanExecuteSupportService.summaryTestPlanReport(item.getId(), false, true);
                }
            });
            testPlanExecuteSupportService.summaryTestPlanReport(testPlanReportId, true, true);
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
            TestPlanExecutionQueue nextTestPlanQueue = testPlanExecuteSupportService.getNextQueue(testPlanReportId, QUEUE_PREFIX_TEST_PLAN_CASE_TYPE);
            testPlanExecuteSupportService.summaryTestPlanReport(testPlanReportId, false, true);
            if (nextTestPlanQueue == null || !StringUtils.equalsAnyIgnoreCase(nextTestPlanQueue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
                testPlanExecuteSupportService.updateReportStopped(testPlanReportId);
            } else {
                this.deepDeleteTestPlanCaseType(testPlanReport);
                this.testPlanExecuteQueueFinish(nextTestPlanQueue.getParentQueueId(), nextTestPlanQueue.getParentQueueType());
            }
        }
    }

    private void deepDeleteTestPlanCaseType(TestPlanReport report) {
        // 删除该任务相关的队列
        testPlanExecuteSupportService.keys("*" + report.getId() + "*")
                .forEach(key -> testPlanExecuteSupportService.deleteQueue(key));
    }

    /**
     * 单个执行测试计划
     * 这里涉及到嵌套查询，不使用事务。中间涉及到的报告生成、测试计划字段更改、测试报告汇总等操作会开启子事务处理。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public String singleExecuteTestPlan(TestPlanExecuteRequest request, String reportId, String userId) {
        String queueId = IDGenerator.nextStr();
        TestPlanExecutionQueue singleExecuteRootQueue = new TestPlanExecutionQueue(
                0,
                userId,
                System.currentTimeMillis(),
                queueId,
                QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE,
                null,
                null,
                request.getExecuteId(),
                request.getRunMode(),
                request.getExecutionSource(),
                reportId,
                IDGenerator.nextStr(),
                false
        );

        testPlanExecuteSupportService.setRedisForList(
                testPlanExecuteSupportService.genQueueKey(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), List.of(JSON.toJSONString(singleExecuteRootQueue)));
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        LogUtils.info("测试计划（组）的单独执行start！计划报告[{}] , 资源ID[{}]", singleExecuteRootQueue.getPrepareReportId(), singleExecuteRootQueue.getSourceID());
        executeTestPlanOrGroup(nextQueue);
        return reportId;
    }

    /**
     * 批量执行测试计划
     * 这里涉及到嵌套查询，不使用事务。中间涉及到的报告生成、测试计划字段更改、测试报告汇总等操作会开启子事务处理。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
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
                                    IDGenerator.nextStr(),
                                    IDGenerator.nextStr(),
                                    false
                            )
                    );
                }
            }
            testPlanExecuteSupportService.setRedisForList(
                    testPlanExecuteSupportService.genQueueKey(queueId, queueType), testPlanExecutionQueues.stream().map(JSON::toJSONString).toList());
            LogUtils.info("测试计划（组）的批量执行start！队列ID[{}] ,队列类型[{}] , 资源ID[{}]", queueId, queueType, JSON.toJSONString(rightfulIds));
            if (StringUtils.equalsIgnoreCase(request.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                //串行
                TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, queueType);
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

        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        TestPlanReportGenRequest genReportRequest = new TestPlanReportGenRequest();
        genReportRequest.setTriggerMode(executionQueue.getExecutionSource());
        genReportRequest.setTestPlanId(executionQueue.getSourceID());
        genReportRequest.setProjectId(testPlan.getProjectId());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {

            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());
            List<String> childPlanIds = children.stream().map(TestPlan::getId).toList();
            Integer caseTotal = 0;
            if (CollectionUtils.isNotEmpty(childPlanIds)) {
                caseTotal = extTestPlanApiCaseMapper.countByPlanIds(childPlanIds) +
                        extTestPlanApiScenarioMapper.countByPlanIds(childPlanIds);
            }
            // 初始化任务
            ExecTask execTask = initExecTask(executionQueue, caseTotal, testPlan, project);

            // 预生成计划组报告
            Map<String, String> reportMap = testPlanReportService.genReportByExecution(executionQueue.getPrepareReportId(), execTask.getId(), genReportRequest, executionQueue.getCreateUser());

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
                                reportMap.get(child.getId()),
                                executionQueue.getTaskId(),
                                executionQueue.isRerun()
                        )
                );
            }

            LogUtils.info("计划组的执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}]", queueId, queueType, executionQueue.getParentQueueId(), executionQueue.getParentQueueType());
            if (CollectionUtils.isEmpty(childrenQueue)) {
                //本次的测试计划组执行完成
                this.testPlanGroupQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
            } else {
                //更改测试计划组的状态
                testPlanService.setExecuteConfig(executionQueue.getSourceID(), executionQueue.getPrepareReportId());
                testPlanExecuteSupportService.setRedisForList(testPlanExecuteSupportService.genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

                if (StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行
                    TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, queueType);
                    testPlanService.setExecuteConfig(nextQueue.getSourceID(), nextQueue.getPrepareReportId());
                    executeTestPlan(nextQueue);
                } else {
                    //并行
                    childrenQueue.forEach(childQueue -> {
                        testPlanService.setExecuteConfig(childQueue.getSourceID(), childQueue.getPrepareReportId());
                        executeTestPlan(childQueue);
                    });
                }
            }

            return executionQueue.getPrepareReportId();
        } else {
            Integer caseTotal = extTestPlanApiCaseMapper.countByPlanIds(List.of(testPlan.getId())) +
                    extTestPlanApiScenarioMapper.countByPlanIds(List.of(testPlan.getId()));
            // 初始化任务
            ExecTask execTask = initExecTask(executionQueue, caseTotal, testPlan, project);

            Map<String, String> reportMap = testPlanReportService.genReportByExecution(executionQueue.getPrepareReportId(), execTask.getId(), genReportRequest, executionQueue.getCreateUser());
            executionQueue.setPrepareReportId(reportMap.get(executionQueue.getSourceID()));
            testPlanService.setExecuteConfig(executionQueue.getSourceID(), executionQueue.getPrepareReportId());
            this.executeTestPlan(executionQueue);
            return executionQueue.getPrepareReportId();
        }
    }

    private ExecTask initExecTask(TestPlanExecutionQueue executionQueue, int caseSize, TestPlan testPlan, Project project) {
        ExecTask execTask = apiCommonService.newExecTask(project.getId(), executionQueue.getCreateUser());
        execTask.setId(executionQueue.getTaskId());
        execTask.setCaseCount(Long.valueOf(caseSize));
        execTask.setTaskName(testPlan.getName());
        execTask.setOrganizationId(project.getOrganizationId());
        execTask.setTriggerMode(executionQueue.getExecutionSource());
        execTask.setParallel(StringUtils.equals(executionQueue.getRunMode(), ApiBatchRunMode.PARALLEL.name()));
        execTask.setTaskType(StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) ? ExecTaskType.TEST_PLAN.name() : ExecTaskType.TEST_PLAN_GROUP.name());
        execTask.setResourceId(testPlan.getId());
        baseTaskHubService.insertExecTask(execTask);

        // 创建报告和任务的关联关系
        ApiReportRelateTask apiReportRelateTask = new ApiReportRelateTask();
        apiReportRelateTask.setReportId(executionQueue.getPrepareReportId());
        apiReportRelateTask.setTaskResourceId(execTask.getId());
        apiReportRelateTaskMapper.insertSelective(apiReportRelateTask);
        return execTask;
    }

    //执行测试计划里不同类型的用例  回调：caseTypeExecuteQueueFinish
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void executeTestPlan(TestPlanExecutionQueue executionQueue) {
        boolean testPlanStopped = testPlanExecuteSupportService.checkTestPlanStopped(executionQueue.getPrepareReportId());
        if (testPlanStopped) {
            //测试计划报告状态已停止的话便不再执行。执行下一个队列。
            this.testPlanExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
        } else {
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
                                executionQueue.getPrepareReportId(),
                                executionQueue.getTaskId(),
                                executionQueue.isRerun())
                );
            }
            LogUtils.info("测试计划执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{}]", queueId, queueType, executionQueue.getParentQueueId(), executionQueue.getParentQueueType(), runMode);
            if (CollectionUtils.isEmpty(childrenQueue)) {
                //本次的测试计划组执行完成
                this.testPlanExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
            } else {
                testPlanExecuteSupportService.setRedisForList(testPlanExecuteSupportService.genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

                //开始根据测试计划集合执行测试用例
                if (StringUtils.equalsIgnoreCase(runMode, ApiBatchRunMode.SERIAL.name())) {
                    //串行
                    TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, queueType);
                    this.executeByTestPlanCollection(nextQueue);
                } else {
                    //并行
                    childrenQueue.forEach(childQueue -> {
                        this.executeByTestPlanCollection(childQueue);
                    });
                }
            }
        }
    }

    public String testPlanOrGroupRerun(ExecTask execTask, String userId) {
        String taskId = execTask.getId();
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo(taskId);
        List<ApiReportRelateTask> apiReportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        String reportId;
        if (CollectionUtils.isNotEmpty(apiReportRelateTasks)) {
            reportId = apiReportRelateTasks.getFirst().getReportId();
        } else {
            // 报告被删除，生成虚拟ID
            reportId = IDGenerator.nextStr();
        }

        String queueId = IDGenerator.nextStr();
        TestPlanExecutionQueue singleExecuteRootQueue = new TestPlanExecutionQueue(
                0,
                userId,
                System.currentTimeMillis(),
                queueId,
                QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE,
                null,
                null,
                execTask.getResourceId(),
                BooleanUtils.isTrue(execTask.getParallel()) ? ApiBatchRunMode.PARALLEL.name() : ApiBatchRunMode.SERIAL.name(),
                TaskTriggerMode.MANUAL.name(),
                reportId,
                taskId,
                true
        );

        testPlanExecuteSupportService.setRedisForList(
                testPlanExecuteSupportService.genQueueKey(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE), List.of(JSON.toJSONString(singleExecuteRootQueue)));
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE);
        LogUtils.info("测试计划（组）重跑！计划报告[{}] , 资源ID[{}]", singleExecuteRootQueue.getPrepareReportId(), singleExecuteRootQueue.getSourceID());
        doTestPlanOrGroupRerun(nextQueue);
        return reportId;
    }

    private String doTestPlanOrGroupRerun(TestPlanExecutionQueue executionQueue) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(executionQueue.getSourceID());
        if (testPlan == null || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException("test_plan.error");
        }

        String prepareReportId = executionQueue.getPrepareReportId();

        testPlanReportService.resetRerunReport(prepareReportId);

        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            List<TestPlan> children = testPlanService.selectNotArchivedChildren(testPlan.getId());
            long pos = 0;
            List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();
            String queueType = QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE;
            String queueId = prepareReportId;

            TestPlanReportExample example = new TestPlanReportExample();
            example.createCriteria().andParentIdEqualTo(prepareReportId);
            Map<String, String> planReportMap = testPlanReportMapper.selectByExample(example).stream().
                    collect(Collectors.toMap(TestPlanReport::getTestPlanId, TestPlanReport::getId));

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
                                planReportMap.get(child.getId()) == null ? IDGenerator.nextStr() : planReportMap.get(child.getId()),
                                executionQueue.getTaskId(),
                                executionQueue.isRerun()
                        )
                );
            }

            LogUtils.info("计划组重跑 --- 队列ID[{}],队列类型[{}]", queueId, queueType);
            if (CollectionUtils.isEmpty(childrenQueue)) {
                //本次的测试计划组执行完成
                this.testPlanGroupQueueFinish(queueId, queueType);
            } else {
                testPlanExecuteSupportService.setRedisForList(testPlanExecuteSupportService.genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());

                if (StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行
                    TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, queueType);
                    executeTestPlan(nextQueue);
                } else {
                    //并行
                    childrenQueue.forEach(childQueue -> executeTestPlan(childQueue));
                }
            }

        } else {
            this.executeTestPlan(executionQueue);
        }
        return prepareReportId;
    }

    //执行测试集 -- 回调：collectionExecuteQueueFinish
    private void executeByTestPlanCollection(TestPlanExecutionQueue executionQueue) {
        TestPlanCollection parentCollection = testPlanCollectionMapper.selectByPrimaryKey(executionQueue.getSourceID());
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andParentIdEqualTo(executionQueue.getSourceID());
        example.setOrderByClause("pos asc");
        List<TestPlanCollection> childrenList = testPlanCollectionMapper.selectByExample(example);

        int pos = 0;
        List<TestPlanExecutionQueue> childrenQueue = new ArrayList<>();

        String queueId = executionQueue.getPrepareReportId() + "_" + parentCollection.getId();
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
        String runMode = parentCollection.getExecuteMethod();
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
                            runMode,
                            executionQueue.getExecutionSource(),
                            executionQueue.getPrepareReportId(),
                            executionQueue.getTaskId(),
                            executionQueue.isRerun()) {{
                        this.setTestPlanCollectionJson(JSON.toJSONString(collection));
                    }}
            );
        }
        LogUtils.info("测试计划不同用例类型的执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{}]", executionQueue.getQueueId(), executionQueue.getQueueType(), executionQueue.getParentQueueId(), executionQueue.getParentQueueType(), executionQueue.getRunMode());
        if (CollectionUtils.isEmpty(childrenQueue)) {
            //本次的测试集执行完成
            this.caseTypeExecuteQueueFinish(executionQueue.getQueueId(), executionQueue.getQueueType());
        } else {
            testPlanExecuteSupportService.setRedisForList(
                    testPlanExecuteSupportService.genQueueKey(queueId, queueType), childrenQueue.stream().map(JSON::toJSONString).toList());
            if (StringUtils.equalsIgnoreCase(runMode, ApiBatchRunMode.SERIAL.name())) {
                //串行
                TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueId, queueType);
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
        LogUtils.info("测试集执行节点 --- 队列ID[{}],队列类型[{}],父队列ID[{}],父队列类型[{}],执行模式[{},资源ID[{}]",
                queueId, testPlanExecutionQueue.getQueueType(), testPlanExecutionQueue.getParentQueueId(), testPlanExecutionQueue.getParentQueueType(),
                testPlanExecutionQueue.getRunMode(), testPlanExecutionQueue.getSourceID());
        boolean execOver = false;
        try {
            TestPlanCollection collection = JSON.parseObject(testPlanExecutionQueue.getTestPlanCollectionJson(), TestPlanCollection.class);
            TestPlanCollection extendedRootCollection = testPlanApiBatchRunBaseService.getExtendedRootCollection(collection);
            String executeMethod = extendedRootCollection == null ? collection.getExecuteMethod() : extendedRootCollection.getExecuteMethod();
            if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.API_CASE.getKey())) {
                if (isParallel(executeMethod)) {
                    execOver = planRunTestPlanApiCaseService.parallelExecute(testPlanExecutionQueue);
                } else {
                    execOver = planRunTestPlanApiCaseService.serialExecute(testPlanExecutionQueue);
                }
            } else if (StringUtils.equalsIgnoreCase(collection.getType(), CaseType.SCENARIO_CASE.getKey())) {
                if (isParallel(executeMethod)) {
                    execOver = planRunTestPlanApiScenarioService.parallelExecute(testPlanExecutionQueue);
                } else {
                    execOver = planRunTestPlanApiScenarioService.serialExecute(testPlanExecutionQueue);
                }
            }
        } catch (Exception e) {
            Log.error("按测试集执行失败!", e);
            execOver = true;
        }

        if (execOver) {
            // 如果没有要执行的用例（可能会出现空测试集的情况），直接调用回调
            collectionExecuteQueueFinish(queueId, false);
        }
    }

    private boolean isParallel(String executeMethod) {
        return StringUtils.equals(executeMethod, ApiBatchRunMode.PARALLEL.name());
    }

    //测试集执行完成
    public void collectionExecuteQueueFinish(String paramQueueId, boolean isStopOnFailure) {
        LogUtils.info("收到测试集执行完成的信息： [{}]", paramQueueId);
        String queueID = paramQueueId;
        String queueType = QUEUE_PREFIX_TEST_PLAN_COLLECTION;
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueID, queueType);
        if (nextQueue == null) {
            LogUtils.info("没有获取到下一个执行节点！ 原始ID[{}]，队列ID[{}]", paramQueueId, queueID);
            return;
        }
        LogUtils.info("获取执行节点完成： 队列ID[{}],队列类型[{},串并行:[{}]执行是否结束[{}],是否是最后一个[{}],当前查出节点的资源ID[{}]]，下一个节点的执行工作准备中...",
                queueID, queueType, nextQueue.getRunMode(), nextQueue.isExecuteFinish(), nextQueue.isLastOne(), nextQueue.getSourceID());
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                boolean execError = false;
                try {
                    LogUtils.info("测试集该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue, isStopOnFailure);
                } catch (Exception e) {
                    Log.error("测试集下一个节点执行失败！", e);
                    execError = true;
                }
                if (execError) {
                    this.collectionExecuteQueueFinish(nextQueue.getQueueId(), true);
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
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueID, queueType);
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            //串行时，由于是先拿出节点再判断执行，所以要判断节点的isExecuteFinish
            if (!nextQueue.isExecuteFinish()) {
                boolean execError = false;
                try {
                    LogUtils.info("用例类型该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue, false);
                } catch (Exception e) {
                    execError = true;
                }
                if (execError) {
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
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueID, queueType);

        /*
        7-23日新增逻辑： 当查找的是测试计划组，并且测试计划组选择的执行方式是并行,在执行之后队列就会删除。此时进行停止，是无法查找到队列的。
        之前不会进行这种查找。但是本迭代在任务中心增加了测试计划组的展示。
        所以之前不会出现的情况， 在这个迭代可以出现了。
        也就是说，在这样的情况下nextQueue是可能为null的
         */
        if (nextQueue == null) {
            return;
        }

        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                boolean execError = false;
                try {
                    LogUtils.info("测试计划该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue, false);
                } catch (Exception e) {
                    execError = true;
                }
                if (execError) {
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
        TestPlanExecutionQueue nextQueue = testPlanExecuteSupportService.getNextQueue(queueID, queueType);
        if (nextQueue == null) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(nextQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
            if (!nextQueue.isExecuteFinish()) {
                boolean execError = false;
                try {
                    LogUtils.info("计划组该节点的串行执行完成！ --- 队列ID[{}],队列类型[{}]，开始执行下一个队列：ID[{}],类型[{}]", queueID, queueType, nextQueue.getQueueId(), nextQueue.getQueueType());
                    this.executeNextNode(nextQueue, false);
                } catch (Exception e) {
                    execError = true;
                }
                if (execError) {
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

    private void executeNextNode(TestPlanExecutionQueue queue, boolean isStopOnFailure) {
        LogUtils.info("开始执行下一个节点： --- 队列ID[{}],队列类型[{}]，预生成报告ID[{}]", queue.getQueueId(), queue.getQueueType(), queue.getPrepareReportId());
        if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
            this.executeTestPlanOrGroup(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
            //执行下一个测试计划之前，将要修改的数据修改一下
            testPlanService.setExecuteConfig(queue.getSourceID(), queue.getPrepareReportId());
            this.executeTestPlan(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
            this.executeByTestPlanCollection(queue);
        } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_COLLECTION)) {
            // 判断是否是失败停止。 如果是失败停止，要检测父类是否也同样配置了失败停止。是的话，不再执行。
            if (this.isCaseTypeExecuteStop(queue.getSourceID(), queue.getPrepareReportId(), isStopOnFailure)) {
                this.collectionExecuteQueueFinish(queue.getQueueId(), isStopOnFailure);
            } else {
                this.executeCase(queue);
            }
        }
    }

    private boolean isCaseTypeExecuteStop(String collectionId, String prepareReportId, boolean isStopOnFailure) {
        boolean caseTypeStopOnFailure = extTestPlanCollectionMapper.getParentStopOnFailure(collectionId);
        // 如果测试集是失败停止触发的，通过父类的配置决定是否执行结束。
        if (isStopOnFailure) {
            return caseTypeStopOnFailure;
        } else if (caseTypeStopOnFailure) {
            // 如果是正常执行结束的，并且配置了失败停止，则根据该测试集的执行结果是否全部成功，来决定是否继续执行

            //首先拿到执行结束的测试集
            List<TestPlanCollection> testPlanCollectionList = extTestPlanCollectionMapper.selectByItemParentId(collectionId);
            TestPlanCollection lastCollection = null;
            for (TestPlanCollection item : testPlanCollectionList) {
                if (StringUtils.equalsIgnoreCase(item.getId(), collectionId)) {
                    break;
                }
                lastCollection = item;
            }

            List<String> execResult = null;
            if (AssociateCaseType.API.equals(lastCollection.getType())) {
                execResult = extTestPlanReportApiCaseMapper.selectExecResultByReportIdAndCollectionId(lastCollection.getId(), prepareReportId);
            } else if (AssociateCaseType.SCENARIO.equals(lastCollection.getType())) {
                execResult = extTestPlanReportApiScenarioMapper.selectExecResultByReportIdAndCollectionId(lastCollection.getId(), prepareReportId);
            }
            return CollectionUtils.size(execResult) != 1 || !StringUtils.equalsIgnoreCase(Objects.requireNonNull(execResult).getFirst(), ResultStatus.SUCCESS.name());
        } else {
            return false;
        }
    }

    private void queueExecuteFinish(TestPlanExecutionQueue queue) {
        LogUtils.info("当前节点执行完成： --- 队列ID[{}],队列类型[{}]，父队列ID[{}]，父队列类型[{}]", queue.getQueueId(), queue.getQueueType(), queue.getParentQueueId(), queue.getParentQueueType());
        if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE)) {
            if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
                // 计划组报告汇总并统计
                finishTaskAndSummaryTestPlanReport(queue.getTaskId(), queue.getQueueId(), true, false);
            } else if (StringUtils.equalsIgnoreCase(queue.getQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
                /*
                    此时处于批量勾选执行中的游离态测试计划执行。所以队列顺序为：QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE -> QUEUE_PREFIX_TEST_PLAN_CASE_TYPE。
                    此时queue节点为testPlanCollection的节点。  而测试计划节点（串行状态下）在执行之前就被弹出了。
                    所以获取报告ID的方式为读取queueId （caseType队列和collection队列的queueId都是报告ID）
                 */
                finishTaskAndSummaryTestPlanReport(queue.getTaskId(), queue.getQueueId(), false, false);
            }
            this.testPlanGroupQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        } else if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE)) {
            // 计划报告汇总并统计
            finishTaskAndSummaryTestPlanReport(queue.getTaskId(), queue.getQueueId(), false, false);
            this.testPlanExecuteQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        } else if (StringUtils.equalsIgnoreCase(queue.getParentQueueType(), QUEUE_PREFIX_TEST_PLAN_CASE_TYPE)) {
            this.caseTypeExecuteQueueFinish(queue.getParentQueueId(), queue.getParentQueueType());
        }
    }

    public void finishTaskAndSummaryTestPlanReport(String taskId, String reportId, boolean isGroupReport, boolean isStop) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(reportId);
        if (StringUtils.equals(testPlanReport.getParentId(), reportId) && StringUtils.isNotBlank(taskId)) {
            // 执行完成，更新任务状态
            apiBatchRunBaseService.updateTaskCompletedStatus(taskId);
        }
        testPlanExecuteSupportService.summaryTestPlanReport(reportId, isGroupReport, isStop);
    }
}
