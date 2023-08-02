package io.metersphere.plan.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.BaseApiExecutionQueueMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.TestPlanExecuteCaseType;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plan.exec.queue.DBTestQueue;
import io.metersphere.request.RunTestPlanRequest;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerfQueueService {
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private PerfModeExecService perfModeExecService;
    @Resource
    protected ApiExecutionQueueMapper queueMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private BaseApiExecutionQueueMapper extApiExecutionQueueMapper;

    /**
     * 性能测试监听检查
     *
     * @param loadTestReport
     */
    public void queueNext(LoadTestReport loadTestReport) {
        LoggerUtil.info("进入结果处理监听", loadTestReport.getId());

        ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
        detailExample.createCriteria().andReportIdEqualTo(loadTestReport.getId());
        List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(detailExample);
        executionQueueDetailMapper.deleteByExample(detailExample);

        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<String> ids = details.stream().map(ApiExecutionQueueDetail::getQueueId).collect(Collectors.toList());
        ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
        queueExample.createCriteria().andIdIn(ids);
        List<ApiExecutionQueue> queues = queueMapper.selectByExample(queueExample);

        List<String> testPlanReportIds = queues.stream().map(ApiExecutionQueue::getReportId).collect(Collectors.toList());
        for (String testPlanReportId : testPlanReportIds) {
            LoggerUtil.info("处理测试计划报告状态", loadTestReport.getId());
            checkTestPlanLoadCaseExecOver(null, testPlanReportId);
        }

        for (ApiExecutionQueueDetail detail : details) {
            // 更新测试计划关联数据状态
            TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
            example.createCriteria().andIdEqualTo(loadTestReport.getTestId());
            if (testPlanLoadCaseMapper.countByExample(example) > 0) {
                TestPlanLoadCaseWithBLOBs loadCase = new TestPlanLoadCaseWithBLOBs();
                loadCase.setId(loadTestReport.getTestId());
                loadCase.setStatus(TestPlanLoadCaseStatus.success.name());
                testPlanLoadCaseMapper.updateByPrimaryKeySelective(loadCase);
                if (CollectionUtils.isNotEmpty(testPlanReportIds)) {
                    checkTestPlanLoadCaseExecOver(loadCase.getId(), null);
                }
            }

            // 检查队列是否已空
            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(detail.getQueueId());
            long count = executionQueueDetailMapper.countByExample(queueDetailExample);
            // 最后一个执行节点，删除执行链
            if (count == 0) {
                queueMapper.deleteByPrimaryKey(detail.getQueueId());
                continue;
            }
            // 获取串行下一个执行节点
            DBTestQueue executionQueue = this.handleQueue(detail.getQueueId(), detail.getTestId());
            if (executionQueue != null && executionQueue.getDetail() != null
                    && StringUtils.isNotEmpty(executionQueue.getDetail().getTestId()) &&
                    StringUtils.equals(executionQueue.getRunMode(), RunModeConstants.SERIAL.toString())) {

                LoggerUtil.info("获取下一个执行资源：" + executionQueue.getDetail().getTestId(), loadTestReport.getId());
                RunTestPlanRequest request = new RunTestPlanRequest();
                request.setTestPlanLoadId(executionQueue.getDetail().getTestId());
                request.setReportId(executionQueue.getDetail().getReportId());
                // 串行执行trigger_mode 是 null
                request.setTriggerMode(TriggerMode.BATCH.name());
                try {
                    perfModeExecService.serial(request);
                } catch (Exception e) {
                    if (BooleanUtils.isTrue(executionQueue.getFailure()) && StringUtils.isNotEmpty(executionQueue.getCompletedReportId())) {
                        LoggerUtil.info("失败停止处理：" + request.getId(), request.getReportId());
                        continue;
                    }
                    LoggerUtil.error("执行异常", executionQueue.getDetail().getTestId(), e);
                    executionQueueDetailMapper.deleteByExample(detailExample);
                    // 异常执行下一个
                    DBTestQueue next = this.handleQueue(executionQueue.getId(), executionQueue.getDetail().getTestId());
                    if (next != null) {
                        LoadTestReport report = new LoadTestReport();
                        report.setId(next.getReportId());
                        this.queueNext(report);
                    }
                }
            }
        }
    }

    public void checkTestPlanLoadCaseExecOver(String testId, String testPlanReportId) {
        if (StringUtils.isNotBlank(testPlanReportId)) {
            // 整体执行测试计划报告时触发的
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testPlanReportId);
        } else {
            // 测试计划内调试时触发的
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testId, UUID.randomUUID().toString());
        }

    }

    public DBTestQueue handleQueue(String id, String testId) {
        ApiExecutionQueue executionQueue = queueMapper.selectByPrimaryKey(id);
        DBTestQueue queue = new DBTestQueue();
        if (executionQueue != null) {
            BeanUtils.copyBean(queue, executionQueue);
            LoggerUtil.info("Get the next execution point：【" + id + "】");

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.setOrderByClause("sort asc");
            example.createCriteria().andQueueIdEqualTo(id);
            List<ApiExecutionQueueDetail> queues = executionQueueDetailMapper.selectByExampleWithBLOBs(example);

            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(queues)) {
                // 处理掉当前已经执行完成的资源
                List<ApiExecutionQueueDetail> completedQueues = queues.stream().filter(item -> StringUtils.equals(item.getTestId(), testId)).collect(Collectors.toList());
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(completedQueues)) {
                    ApiExecutionQueueDetail completed = completedQueues.get(0);
                    queue.setCompletedReportId(completed.getReportId());
                    executionQueueDetailMapper.deleteByPrimaryKey(completed.getId());
                    queues.remove(completed);
                }
                // 取出下一个要执行的节点
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(queues)) {
                    queue.setDetail(queues.get(0));
                } else {
                    LoggerUtil.info("execution complete,clear queue：【" + id + "】");
                    queueMapper.deleteByPrimaryKey(id);
                }
            } else {
                LoggerUtil.info("execution complete,clear queue：【" + id + "】");
                queueMapper.deleteByPrimaryKey(id);
            }
        } else {
            LoggerUtil.info("The queue was accidentally deleted：【" + id + "】");
        }
        return queue;
    }

    @Resource
    private RedisTemplateService redisTemplateService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DBTestQueue add(Object runObj, String poolId, String testPlanReportId, String reportType, String runMode, RunModeConfigDTO config) {
        LoggerUtil.info("报告【" + testPlanReportId + "】开始生成执行链");
        if (config.getEnvMap() == null) {
            config.setEnvMap(new LinkedHashMap<>());
        }
        ApiExecutionQueue executionQueue = getApiExecutionQueue(poolId, testPlanReportId, reportType, runMode, config);
        queueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);

        Map<String, String> detailMap = new HashMap<>();
        List<ApiExecutionQueueDetail> queueDetails = new LinkedList<>();
        Map<String, String> requests = (Map<String, String>) runObj;

        initPerf(requests, resQueue, config, detailMap, queueDetails);

        if (CollectionUtils.isNotEmpty(queueDetails)) {
            extApiExecutionQueueMapper.sqlInsert(queueDetails);
        }
        resQueue.setDetailMap(detailMap);
        LoggerUtil.info("报告【" + testPlanReportId + "】生成执行链结束");
        //移除Redis中的标志
        redisTemplateService.unlock(testPlanReportId, TestPlanExecuteCaseType.LOAD_CASE.name(), testPlanReportId);
        return resQueue;
    }

    protected ApiExecutionQueue getApiExecutionQueue(String poolId, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        ApiExecutionQueue executionQueue = new ApiExecutionQueue();
        executionQueue.setId(UUID.randomUUID().toString());
        executionQueue.setCreateTime(System.currentTimeMillis());
        executionQueue.setPoolId(poolId);
        executionQueue.setFailure(config.isOnSampleError());
        executionQueue.setReportId(reportId);
        executionQueue.setReportType(TestPlanExecuteCaseType.LOAD_CASE.name());
        executionQueue.setRunMode(runMode);
        return executionQueue;
    }

    private void initPerf(Map<String, String> requests, DBTestQueue resQueue, RunModeConfigDTO config, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        String envStr = JSON.toJSONString(config.getEnvMap());
        int i = 0;
        for (String testId : requests.keySet()) {
            ApiExecutionQueueDetail queue = detail(requests.get(testId), testId, config.getMode(), i++, resQueue.getId(), envStr);
            if (i == 1) {
                resQueue.setDetail(queue);
            }
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            queueDetails.add(queue);
            detailMap.put(testId, queue.getId());
        }
    }

    protected ApiExecutionQueueDetail detail(String reportId, String testId, String type, int sort, String queueId, String envMap) {
        ApiExecutionQueueDetail queue = new ApiExecutionQueueDetail();
        queue.setCreateTime(System.currentTimeMillis());
        queue.setId(UUID.randomUUID().toString());
        queue.setEvnMap(envMap);
        queue.setReportId(reportId);
        queue.setTestId(testId);
        queue.setType(type);
        queue.setSort(sort);
        queue.setQueueId(queueId);
        return queue;
    }


}
