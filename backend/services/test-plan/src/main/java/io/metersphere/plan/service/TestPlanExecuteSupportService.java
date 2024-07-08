package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanReportPostParam;
import io.metersphere.plan.mapper.TestPlanReportApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.dto.queue.TestPlanExecutionQueue;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanExecuteSupportService {

    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private TestPlanService testPlanService;

    public static final String QUEUE_PREFIX_TEST_PLAN_BATCH_EXECUTE = "test-plan-batch-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_GROUP_EXECUTE = "test-plan-group-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_CASE_TYPE = "test-plan-case-type-execute:";
    public static final String QUEUE_PREFIX_TEST_PLAN_COLLECTION = "test-plan-collection-execute:";

    public static final String LAST_QUEUE_PREFIX = "last-queue:";
    @Resource
    private TestPlanReportMapper testPlanReportMapper;


    public void setRedisForList(String key, List<String> list) {
        stringRedisTemplate.opsForList().rightPushAll(key, list);
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    public void deleteRedisKey(String redisKey) {
        //清除list的key 和 last key节点
        stringRedisTemplate.delete(redisKey);
        stringRedisTemplate.delete(genQueueKey(redisKey, LAST_QUEUE_PREFIX));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void summaryTestPlanReport(String reportId, boolean isGroupReport, boolean isStop) {
        LogUtils.info("开始合并报告： --- 报告ID[{}],是否是报告组[{}]", reportId, isGroupReport);
        try {
            // 如果是停止的计划任务, 报告用例状态改成STOPPED
            if (isStop) {
                updateReportDetailStopped(reportId);
            }

            if (isGroupReport) {
                testPlanReportService.summaryGroupReport(reportId);
            } else {
                //汇总之前，根据测试计划设置，检查是否要同步修改功能用例的状态
                testPlanService.autoUpdateFunctionalCase(reportId);
                //进行统计汇总
                testPlanReportService.summaryPlanReport(reportId);
            }

            TestPlanReportPostParam postParam = new TestPlanReportPostParam();
            postParam.setReportId(reportId);
            // 执行生成报告, 执行状态为已完成, 执行及结束时间为当前时间
            postParam.setEndTime(System.currentTimeMillis());
            postParam.setExecStatus(isStop ? ExecStatus.STOPPED.name() : ExecStatus.COMPLETED.name());
            testPlanReportService.postHandleReport(postParam, false);
        } catch (Exception e) {
            LogUtils.error("测试计划报告汇总失败!reportId:" + reportId, e);
            TestPlanReport stopReport = testPlanReportService.selectById(reportId);
            stopReport.setId(reportId);
            stopReport.setExecStatus(ResultStatus.ERROR.name());
            stopReport.setEndTime(System.currentTimeMillis());
            testPlanReportMapper.updateByPrimaryKeySelective(stopReport);
        }
    }


    /**
     * 获取下一个队列节点
     */
    public TestPlanExecutionQueue getNextQueue(String queueId, String queueType) {
        if (StringUtils.isAnyBlank(queueId, queueType)) {
            return null;
        }

        String queueKey = this.genQueueKey(queueId, queueType);
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
        String queueDetail = listOps.leftPop(queueKey);
        if (StringUtils.isBlank(queueDetail)) {
            // 重试1次获取
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
            queueDetail = stringRedisTemplate.opsForList().leftPop(queueKey);
        }

        if (StringUtils.isNotBlank(queueDetail)) {
            TestPlanExecutionQueue returnQueue = JSON.parseObject(queueDetail, TestPlanExecutionQueue.class);
            Long size = listOps.size(queueKey);
            if (size == null || size == 0) {
                returnQueue.setLastOne(true);
                if (StringUtils.equalsIgnoreCase(returnQueue.getRunMode(), ApiBatchRunMode.SERIAL.name())) {
                    //串行的执行方式意味着最后一个节点要单独存储
                    stringRedisTemplate.opsForValue().setIfAbsent(genQueueKey(queueKey, LAST_QUEUE_PREFIX), JSON.toJSONString(returnQueue), 1, TimeUnit.DAYS);
                }
                // 最后一个节点清理队列
                deleteQueue(queueKey);
            }
            return returnQueue;
        } else {
            String lastQueueJson = stringRedisTemplate.opsForValue().getAndDelete(genQueueKey(queueKey, LAST_QUEUE_PREFIX));
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


    public Boolean deleteQueue(String queueKey) {
       return stringRedisTemplate.delete(queueKey);
    }

    public Set<String> keys(String queueKey) {
        return stringRedisTemplate.keys(queueKey);
    }

    //生成队列key
    public String genQueueKey(String queueId, String queueType) {
        return queueType + queueId;
    }

    public boolean checkTestPlanStopped(String prepareReportId) {
        TestPlanReportExample reportExample = new TestPlanReportExample();
        reportExample.createCriteria().andIdEqualTo(prepareReportId).andExecStatusEqualTo(ExecStatus.STOPPED.name());
        return testPlanReportMapper.countByExample(reportExample) > 0;
    }

    public void updateReportStopped(String testPlanReportId) {
        TestPlanReport testPlanReport = new TestPlanReport();
        testPlanReport.setId(testPlanReportId);
        testPlanReport.setExecStatus(ExecStatus.STOPPED.name());
        testPlanReportMapper.updateByPrimaryKeySelective(testPlanReport);

        // 用例明细未执行的更新为Stopped
        updateReportDetailStopped(testPlanReportId);
    }

    public void updateReportDetailStopped(String testPlanReportId) {
        TestPlanReportApiCaseExample apiCaseExample = new TestPlanReportApiCaseExample();
        apiCaseExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId).andApiCaseExecuteResultIsNull();
        TestPlanReportApiCase testPlanReportApiCase = new TestPlanReportApiCase();
        testPlanReportApiCase.setApiCaseExecuteResult(ExecStatus.STOPPED.name());
        testPlanReportApiCaseMapper.updateByExampleSelective(testPlanReportApiCase, apiCaseExample);

        TestPlanReportApiScenarioExample scenarioExample = new TestPlanReportApiScenarioExample();
        scenarioExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId).andApiScenarioExecuteResultIsNull();
        TestPlanReportApiScenario testPlanReportApiScenario = new TestPlanReportApiScenario();
        testPlanReportApiScenario.setApiScenarioExecuteResult(ExecStatus.STOPPED.name());
        testPlanReportApiScenarioMapper.updateByExampleSelective(testPlanReportApiScenario, scenarioExample);
    }
}
