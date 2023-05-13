package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.UiExecutionQueueParam;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.JmeterThreadUtils;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportResultExample;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiExecutionQueueMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiExecutionQueueService {
    @Resource
    protected ApiExecutionQueueMapper queueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
    @Resource
    private RedisTemplateService redisTemplateService;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private UiScenarioSerialServiceProxy uiScenarioSerialServiceProxy;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    protected ExtApiExecutionQueueMapper extApiExecutionQueueMapper;
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Lazy
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private RemakeReportService remakeReportService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        LoggerUtil.info("开始生成执行链", reportId);

        ApiExecutionQueue executionQueue = getApiExecutionQueue(poolId, reportId, reportType, runMode, config);
        queueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);
        Map<String, String> detailMap = new HashMap<>();
        List<ApiExecutionQueueDetail> queueDetails = new LinkedList<>();
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name())) {
            final int[] sort = {0};
            Map<String, ApiDefinitionExecResult> runMap = (Map<String, ApiDefinitionExecResult>) runObj;
            if (config.getEnvMap() == null) {
                config.setEnvMap(new LinkedHashMap<>());
            }
            String envStr = JSON.toJSONString(config.getEnvMap());
            runMap.forEach((k, v) -> {
                ApiExecutionQueueDetail queue = detail(v.getId(), k, config.getMode(), sort[0], executionQueue.getId(), envStr);
                if (sort[0] == 0) {
                    resQueue.setQueue(queue);
                }
                sort[0]++;
                queueDetails.add(queue);
                detailMap.put(k, queue.getId());
            });
        } else if (StringUtils.equalsIgnoreCase(type, ApiRunMode.TEST_PLAN_PERFORMANCE_TEST.name())) {
            final int[] sort = {0};
            Map<String, String> runMap = (Map<String, String>) runObj;
            if (config.getEnvMap() == null) {
                config.setEnvMap(new LinkedHashMap<>());
            }
            String envStr = JSON.toJSONString(config.getEnvMap());
            runMap.forEach((k, v) -> {
                ApiExecutionQueueDetail queue = detail(v, k, "loadTest", sort[0], executionQueue.getId(), envStr);
                if (sort[0] == 0) {
                    resQueue.setQueue(queue);
                }
                sort[0]++;
                queueDetails.add(queue);
                detailMap.put(k, queue.getId());
            });
        } else {
            Map<String, RunModeDataDTO> runMap = (Map<String, RunModeDataDTO>) runObj;
            final int[] sort = {0};
            runMap.forEach((k, v) -> {
                String envMap = JSON.toJSONString(v.getPlanEnvMap());
                if (StringUtils.startsWith(type, "UI_")) {
                    UiExecutionQueueParam param = new UiExecutionQueueParam();
                    BeanUtils.copyBean(param, config);
                    envMap = JSONObject.toJSONString(param);
                }
                ApiExecutionQueueDetail queue = detail(k, v.getTestId(), config.getMode(), sort[0], executionQueue.getId(), envMap);
                queue.setSort(sort[0]);
                if (sort[0] == 0) {
                    resQueue.setQueue(queue);
                }
                sort[0]++;
                queueDetails.add(queue);
                detailMap.put(k, queue.getId());
            });
        }
        if (CollectionUtils.isNotEmpty(queueDetails)) {
            extApiExecutionQueueMapper.sqlInsert(queueDetails);
        }
        resQueue.setDetailMap(detailMap);

        LoggerUtil.info("生成执行链结束", reportId);
        return resQueue;
    }

    protected ApiExecutionQueue getApiExecutionQueue(String poolId, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        ApiExecutionQueue executionQueue = new ApiExecutionQueue();
        executionQueue.setId(UUID.randomUUID().toString());
        executionQueue.setCreateTime(System.currentTimeMillis());
        executionQueue.setPoolId(poolId);
        executionQueue.setFailure(config.isOnSampleError());
        executionQueue.setReportId(reportId);
        executionQueue.setReportType(StringUtils.isNotEmpty(reportType) ? reportType : RunModeConstants.INDEPENDENCE.toString());
        executionQueue.setRunMode(runMode);
        return executionQueue;
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

    private boolean failure(DBTestQueue executionQueue, ResultDTO dto) {
        LoggerUtil.info("进入失败停止处理：" + executionQueue.getId(), dto.getReportId());
        boolean isError = false;
        if (StringUtils.contains(dto.getRunMode(), ApiRunMode.SCENARIO.name())) {
            if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
                example.createCriteria().andReportIdEqualTo(dto.getReportId()).andStatusEqualTo(ExecuteResult.SCENARIO_ERROR.toString());
                long error = apiScenarioReportResultMapper.countByExample(example);
                isError = error > 0;
            } else {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(executionQueue.getCompletedReportId());
                if (report != null && StringUtils.equalsIgnoreCase(report.getStatus(), "Error")) {
                    isError = true;
                }
            }
        } else {
            ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(executionQueue.getCompletedReportId());
            if (result != null && StringUtils.equalsIgnoreCase(result.getStatus(), "Error")) {
                isError = true;
            }
        }
        if (isError) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId());

            if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(dto.getTestPlanReportId(), "Stopped");
            }
            // 更新未执行的报告状态
            List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(example);
            List<String> reportIds = details.stream().map(ApiExecutionQueueDetail::getReportId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(reportIds)) {
                extApiDefinitionExecResultMapper.update(reportIds);
                extApiScenarioReportMapper.update(reportIds);
            }
            // 清除队列
            executionQueueDetailMapper.deleteByExample(example);
            queueMapper.deleteByPrimaryKey(executionQueue.getId());

            if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                String reportId = dto.getReportId();
                if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                    reportId = dto.getTestPlanReportId();
                }
                apiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
            }
            return false;
        }
        return true;
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

            if (CollectionUtils.isNotEmpty(queues)) {
                // 处理掉当前已经执行完成的资源
                List<ApiExecutionQueueDetail> completedQueues = queues.stream().filter(item -> StringUtils.equals(item.getTestId(), testId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(completedQueues)) {
                    ApiExecutionQueueDetail completed = completedQueues.get(0);
                    queue.setCompletedReportId(completed.getReportId());
                    executionQueueDetailMapper.deleteByPrimaryKey(completed.getId());
                    queues.remove(completed);
                }
                // 取出下一个要执行的节点
                if (CollectionUtils.isNotEmpty(queues)) {
                    queue.setQueue(queues.get(0));
                } else {
                    LoggerUtil.info("execution complete,clear queue：【" + id + "】", queue.getReportId());
                    queueMapper.deleteByPrimaryKey(id);
                }
            } else {
                LoggerUtil.info("execution complete,clear queue：【" + id + "】", queue.getReportId());
                queueMapper.deleteByPrimaryKey(id);
            }
        } else {
            LoggerUtil.info("The queue was accidentally deleted：【" + id + "】", queue.getReportId());
        }
        return queue;
    }

    public void testPlanReportTestEnded(String testPlanReportId) {
        // 检查测试计划中其他队列是否结束
        ApiExecutionQueueExample apiExecutionQueueExample = new ApiExecutionQueueExample();
        apiExecutionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
        List<ApiExecutionQueue> queues = queueMapper.selectByExample(apiExecutionQueueExample);
        if (CollectionUtils.isEmpty(queues)) {
            LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
            CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
        } else {
            List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdIn(ids);
            long count = executionQueueDetailMapper.countByExample(example);
            if (count == 0) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(testPlanReportId, TestPlanReportStatus.COMPLETED.name());

                LoggerUtil.info("Clear Queue：" + ids);
                ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
                queueExample.createCriteria().andIdIn(ids);
                queueMapper.deleteByExample(queueExample);
            }
        }
    }

    public void queueNext(ResultDTO dto) {
        LoggerUtil.info("开始处理队列：" + dto.getQueueId(), dto.getReportId());
        if (StringUtils.equals(dto.getRunType(), RunModeConstants.PARALLEL.toString())) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            executionQueueDetailMapper.deleteByExample(example);
            // 检查队列是否已空
            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(dto.getQueueId());
            long count = executionQueueDetailMapper.countByExample(queueDetailExample);
            if (count == 0) {
                queueMapper.deleteByPrimaryKey(dto.getQueueId());
                if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    String reportId = dto.getReportId();
                    if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                        reportId = dto.getTestPlanReportId();
                    }
                    apiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
                }
            }
            return;
        }
        // 获取串行下一个执行节点
        DBTestQueue executionQueue = this.handleQueue(dto.getQueueId(), dto.getTestId());
        if (executionQueue != null) {
            // 串行失败停止
            if (BooleanUtils.isTrue(executionQueue.getFailure()) && StringUtils.isNotEmpty(executionQueue.getCompletedReportId())) {
                boolean isNext = failure(executionQueue, dto);
                if (!isNext) {
                    return;
                }
            }
            LoggerUtil.info("开始处理执行队列：" + executionQueue.getId() + " 当前资源是：" + dto.getTestId(), dto.getReportId());
            if (executionQueue.getQueue() != null && StringUtils.isNotEmpty(executionQueue.getQueue().getTestId())) {
                if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
                    LoggerUtil.info("当前执行队列是：" + JSON.toJSONString(executionQueue.getQueue()));
                    // 防止重复执行
                    String key = StringUtils.join(RunModeConstants.SERIAL.name(), "_", executionQueue.getQueue().getReportId());
                    boolean isNext = redisTemplateService.setIfAbsent(key, executionQueue.getQueue().getQueueId());
                    if (isNext) {
                        if (StringUtils.startsWith(executionQueue.getRunMode(), "UI")) {
                            uiScenarioSerialServiceProxy.serial(executionQueue, executionQueue.getQueue());
                        } else {
                            apiScenarioSerialService.serial(executionQueue, executionQueue.getQueue());
                        }
                    }
                }
            } else {
                if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    String reportId = dto.getReportId();
                    if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                        reportId = dto.getTestPlanReportId();
                    }
                    apiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
                }
                queueMapper.deleteByPrimaryKey(dto.getQueueId());
                LoggerUtil.info("Queue execution ends：" + dto.getQueueId(), dto.getReportId());
            }

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            executionQueueDetailMapper.deleteByExample(example);
        }
        LoggerUtil.info("处理队列结束：" + dto.getQueueId(), dto.getReportId());
    }

    public void defendQueue() {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 30 * SECOND_MILLIS;
        // 计算半小时前的超时报告
        final long timeout = System.currentTimeMillis() - (30 * MINUTE_MILLIS);
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andCreateTimeLessThan(timeout).andTypeNotEqualTo("loadTest");
        List<ApiExecutionQueueDetail> queueDetails = executionQueueDetailMapper.selectByExample(example);

        for (ApiExecutionQueueDetail item : queueDetails) {
            ApiExecutionQueue queue = queueMapper.selectByPrimaryKey(item.getQueueId());
            if (queue == null) {
                executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                continue;
            }
            // 在资源池中执行
            if (StringUtils.isNotEmpty(queue.getPoolId())
                    && jMeterService.getRunningQueue(queue.getPoolId(), item.getReportId())) {
                continue;
            }
            // 检查执行报告是否还在等待队列中或执行线程中
            if (JmeterThreadUtils.isRunning(item.getReportId(), item.getTestId())) {
                continue;
            }
            // 检查是否已经超时
            if (StringUtils.equalsAnyIgnoreCase(queue.getRunMode(),
                    ApiRunMode.SCENARIO.name(),
                    ApiRunMode.SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO.name(),
                    ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                // 报告已经被删除则队列也删除
                if (report == null) {
                    executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                }
                // 这里只处理已经开始执行的队列如果 报告状态是 Waiting 表示还没开始暂不处理
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), TestPlanReportStatus.RUNNING.name())
                        && report.getUpdateTime() < timeout) {
                    JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO();
                    runRequest.setTestId(item.getTestId());
                    runRequest.setReportId(item.getReportId());
                    runRequest.setRunMode(queue.getRunMode());
                    runRequest.setReportType(queue.getReportType());
                    runRequest.setQueueId(queue.getId());
                    runRequest.setPoolId(queue.getPoolId());
                    runRequest.setRunType(item.getType());
                    if (StringUtils.equalsAny(runRequest.getRunMode(),
                            ApiRunMode.SCENARIO_PLAN.name(),
                            ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                            ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                        runRequest.setTestPlanReportId(queue.getReportId());
                    }
                    remakeReportService.testEnded(runRequest, APITestStatus.TIMEOUT.name());
                }
            } else {
                // 用例/接口超时结果处理
                ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(item.getReportId());
                if (result != null && StringUtils.equalsAnyIgnoreCase(result.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                    result.setStatus(ScenarioStatus.Timeout.name());
                    apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                    executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                }
            }
        }
        // 集成报告超时处理
        ApiExecutionQueueExample queueDetailExample = new ApiExecutionQueueExample();
        queueDetailExample.createCriteria().andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString()).andCreateTimeLessThan(timeout);
        List<ApiExecutionQueue> executionQueues = queueMapper.selectByExample(queueDetailExample);
        if (CollectionUtils.isNotEmpty(executionQueues)) {
            executionQueues.forEach(item -> {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), TestPlanReportStatus.RUNNING.name(), APITestStatus.Waiting.name())
                        && (report.getUpdateTime() < timeout)) {
                    report.setStatus(ScenarioStatus.Timeout.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
            });
        }
        // 处理测试计划报告
        List<ApiExecutionQueue> queues = extApiExecutionQueueMapper.findTestPlanReportQueue();
        if (CollectionUtils.isNotEmpty(queues)) {
            queues.forEach(item -> {
                // 更新测试计划报告
                if (StringUtils.isNotEmpty(item.getReportId())) {
                    LoggerUtil.info("Handling test plan reports that are not in the execution queue：【" + item.getReportId() + "】");
                    CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(item.getReportId(), TestPlanReportStatus.COMPLETED.name());
                }
            });
        }

        List<String> testPlanReports = extApiExecutionQueueMapper.findTestPlanRunningReport();
        if (CollectionUtils.isNotEmpty(testPlanReports)) {
            testPlanReports.forEach(reportId -> {
                LoggerUtil.info("Compensation Test Plan Report：【" + reportId + "】");
                CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(reportId, TestPlanReportStatus.COMPLETED.name());
            });
        }
    }

    public void stop(String reportId) {
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(example);
        details.forEach(detail -> {
            executionQueueDetailMapper.deleteByPrimaryKey(detail.getId());

            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(detail.getQueueId());
            long queueDetailSize = executionQueueDetailMapper.countByExample(queueDetailExample);
            if (queueDetailSize <= 1) {
                ApiExecutionQueue queue = queueMapper.selectByPrimaryKey(detail.getQueueId());
                // 更新测试计划报告
                if (queue != null && StringUtils.isNotEmpty(queue.getReportId())) {
                    CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(queue.getReportId(), "Stopped");
                }
            }
        });
    }

    public void stop(List<String> reportIds) {
        if (CollectionUtils.isEmpty(reportIds)) {
            return;
        }
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andReportIdIn(reportIds);
        List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(example);

        List<String> queueIds = new ArrayList<>();
        details.forEach(item -> {
            if (!queueIds.contains(item.getQueueId())) {
                queueIds.add(item.getQueueId());
            }
        });
        executionQueueDetailMapper.deleteByExample(example);

        for (String queueId : queueIds) {
            ApiExecutionQueue queue = queueMapper.selectByPrimaryKey(queueId);
            // 更新测试计划报告
            if (queue != null && StringUtils.isNotEmpty(queue.getReportId())) {
                CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(queue.getReportId(), "Stopped");
                queueMapper.deleteByPrimaryKey(queueId);
            }
        }
    }


    /**
     * 性能测试监听检查
     *
     * @param loadTestReport
     */
    public void checkExecutionQueueByLoadTest(LoadTestReport loadTestReport) {
        ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
        detailExample.createCriteria().andReportIdEqualTo(loadTestReport.getId());
        executionQueueDetailMapper.deleteByExample(detailExample);
        List<String> testPlanReportIdList = testPlanReportService.getTestPlanReportIdsByLoadTestReportId(loadTestReport.getId());
        for (String testPlanReportId : testPlanReportIdList) {
            this.testPlanReportTestEnded(testPlanReportId);
        }
    }

    public void deleteQueue() {
        ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
        queueMapper.deleteByExample(queueExample);

        ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
        executionQueueDetailMapper.deleteByExample(detailExample);
    }
}
