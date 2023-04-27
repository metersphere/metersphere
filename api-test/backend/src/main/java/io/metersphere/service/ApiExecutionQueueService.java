package io.metersphere.service;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.exec.api.ApiCaseSerialService;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.JMeterThreadUtils;
import io.metersphere.api.jmeter.utils.JmxFileUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseApiExecutionQueueMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.services.FileServer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    protected BaseApiExecutionQueueMapper extApiExecutionQueueMapper;
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Resource
    private ApiCaseSerialService apiCaseSerialService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ApiExecutionQueueMapper apiExecutionQueueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper apiExecutionQueueDetailMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode, String triggerMode, RunModeConfigDTO config) {
        LoggerUtil.info("报告【" + type + "】开始生成执行链", reportId);
        if (config.getEnvMap() == null) {
            config.setEnvMap(new LinkedHashMap<>());
        }
        ApiExecutionQueue executionQueue = getApiExecutionQueue(poolId, reportId, reportType, runMode,triggerMode, config);
        queueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);

        Map<String, String> detailMap = new HashMap<>();
        List<ApiExecutionQueueDetail> queueDetails = new LinkedList<>();
        // 初始化API/用例队列
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name())) {
            Map<String, ApiDefinitionExecResult> runMap = (Map<String, ApiDefinitionExecResult>) runObj;
            initApi(runMap, resQueue, config, detailMap, queueDetails);
        }
        // 初始化场景
        else {
            Map<String, RunModeDataDTO> runMap = (Map<String, RunModeDataDTO>) runObj;
            initScenario(runMap, resQueue, config, detailMap, queueDetails);
        }
        if (CollectionUtils.isNotEmpty(queueDetails)) {
            extApiExecutionQueueMapper.sqlInsert(queueDetails);
        }
        resQueue.setDetailMap(detailMap);
        LoggerUtil.info("报告【" + type + "】生成执行链结束", reportId);
        return resQueue;
    }

    private void initScenario(Map<String, RunModeDataDTO> runMap, DBTestQueue resQueue, RunModeConfigDTO config, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        final int[] sort = {0};
        runMap.forEach((k, v) -> {
            String envMap = JSON.toJSONString(v.getPlanEnvMap());
            ApiExecutionQueueDetail queue = detail(k, v.getTestId(), config.getMode(), sort[0], resQueue.getId(), envMap);
            queue.setSort(sort[0]);
            if (sort[0] == 0) {
                resQueue.setDetail(queue);
            }
            sort[0]++;
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            List<String> projectIds = new ArrayList<>();
            // 获取当前所属项目ID用于后续区分资源隔离
            if (MapUtils.isNotEmpty(v.getPlanEnvMap())) {
                List<String> kyList = v.getPlanEnvMap().keySet().stream().collect(Collectors.toList());
                projectIds.addAll(kyList);
            } else {
                projectIds.add(v.getReport().getProjectId());
            }
            queue.setProjectIds(JSON.toJSONString(projectIds));
            queueDetails.add(queue);
            detailMap.put(k, queue.getId());
        });
    }

    private void initApi(Map<String, ApiDefinitionExecResult> runMap, DBTestQueue resQueue, RunModeConfigDTO config, Map<String, String> detailMap, List<ApiExecutionQueueDetail> queueDetails) {
        int sort = 0;
        String envStr = JSON.toJSONString(config.getEnvMap());
        for (String k : runMap.keySet()) {
            ApiExecutionQueueDetail queue = detail(runMap.get(k).getId(), k, config.getMode(), sort++, resQueue.getId(), envStr);
            if (sort == 1) {
                resQueue.setDetail(queue);
            }
            queue.setRetryEnable(config.isRetryEnable());
            queue.setRetryNumber(config.getRetryNum());
            queue.setProjectIds(JSON.toJSONString(new ArrayList<>() {{
                this.add(runMap.get(k).getProjectId());
            }}));
            queueDetails.add(queue);
            detailMap.put(k, queue.getId());
        }
        resQueue.setDetailMap(detailMap);
    }

    protected ApiExecutionQueue getApiExecutionQueue(String poolId, String reportId, String reportType, String runMode, String triggerMode, RunModeConfigDTO config) {
        ApiExecutionQueue executionQueue = new ApiExecutionQueue();
        executionQueue.setId(UUID.randomUUID().toString());
        executionQueue.setCreateTime(System.currentTimeMillis());
        executionQueue.setPoolId(poolId);
        executionQueue.setFailure(config.isOnSampleError());
        executionQueue.setReportId(reportId);
        executionQueue.setReportType(StringUtils.isNotEmpty(reportType) ? reportType : RunModeConstants.INDEPENDENCE.toString());
        executionQueue.setRunMode(runMode);
        executionQueue.setTriggerMode(triggerMode);
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
        LoggerUtil.info("进入失败停止处理：" + executionQueue.getId());
        boolean isError = false;
        if (StringUtils.contains(dto.getRunMode(), ApiRunMode.SCENARIO.name())) {
            if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
                example.createCriteria().andReportIdEqualTo(dto.getReportId()).andStatusEqualTo(ApiReportStatus.ERROR.name());
                long error = apiScenarioReportResultMapper.countByExample(example);
                isError = error > 0;
            } else {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(executionQueue.getCompletedReportId());
                if (report != null && StringUtils.equalsIgnoreCase(report.getStatus(), ApiReportStatus.ERROR.name())) {
                    isError = true;
                }
            }
        } else {
            ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(executionQueue.getCompletedReportId());
            if (result != null && StringUtils.equalsIgnoreCase(result.getStatus(), ApiReportStatus.ERROR.name())) {
                isError = true;
            }
        }
        if (isError) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId());
            checkTestPlanCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
            // 更新未执行的报告状态
            List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(example);
            List<String> reportIds = details.stream().map(ApiExecutionQueueDetail::getReportId).collect(Collectors.toList());
            List<String> testIds = details.stream().map(ApiExecutionQueueDetail::getTestId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(reportIds)) {
                extApiDefinitionExecResultMapper.update(reportIds);
                extApiScenarioReportMapper.update(reportIds);
            }
            if (CollectionUtils.isNotEmpty(testIds)) {
                extTestPlanApiCaseMapper.updateStatusStop(testIds);
            }
            // 清除队列
            executionQueueDetailMapper.deleteByExample(example);
            queueMapper.deleteByPrimaryKey(executionQueue.getId());
            // 集合报告合并
            this.margeReport(dto);
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

    private void testPlanCaseTestEnd(String testId, String runMode) {
        //不是整体测试计划执行的用例，发送testID给测试跟踪模块，用于做单接口执行后续操作处理
        if (StringUtils.isNotEmpty(testId) && StringUtils.equalsAnyIgnoreCase(runMode,
                ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testId, UUID.randomUUID().toString());
        }
    }

    public void checkTestPlanCaseTestEnd(String testId, String runMode, String testPlanReportId) {
        if (StringUtils.isEmpty(testPlanReportId)) {
            //不是整体测试计划执行的用例，发送testID给测试跟踪模块，用于做单接口执行后续操作处理
            this.testPlanCaseTestEnd(testId, runMode);
        } else {
            // 由测试计划检查测试计划中其他队列是否结束
            ApiExecutionQueueExample executionQueueExample = new ApiExecutionQueueExample();
            executionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
            List<ApiExecutionQueue> queues = apiExecutionQueueMapper.selectByExample(executionQueueExample);
            if (CollectionUtils.isEmpty(queues)) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testPlanReportId);
            } else {
                List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
                ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
                detailExample.createCriteria().andQueueIdIn(ids);
                long count = apiExecutionQueueDetailMapper.countByExample(detailExample);
                if (count == 0) {
                    kafkaTemplate.send(KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, testPlanReportId);
                }
            }
        }
    }

    public void queueNext(ResultDTO dto) {
        LoggerUtil.info("开始处理队列：" + dto.getReportId() + "QID：" + dto.getQueueId());
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
                // 集合报告合并
                this.margeReport(dto);
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
            LoggerUtil.info("开始处理执行队列：" + executionQueue.getId() + " 当前资源是：" + dto.getTestId() + "报告ID：" + dto.getReportId());
            if (executionQueue.getDetail() != null && StringUtils.isNotEmpty(executionQueue.getDetail().getTestId())) {
                if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
                    LoggerUtil.info("当前执行队列是：" + JSON.toJSONString(executionQueue.getDetail()));
                    // 防止重复执行
                    String key = StringUtils.join(RunModeConstants.SERIAL.name(), "_", executionQueue.getDetail().getReportId());
                    boolean isNext = redisTemplateService.setIfAbsent(key, executionQueue.getDetail().getQueueId());
                    if (!isNext) {
                        return;
                    }
                    redisTemplateService.expire(key);
                    if (StringUtils.equalsAny(executionQueue.getRunMode(),
                            ApiRunMode.SCENARIO.name(),
                            ApiRunMode.SCENARIO_PLAN.name(),
                            ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                            ApiRunMode.SCHEDULE_SCENARIO.name(),
                            ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                        apiScenarioSerialService.serial(executionQueue);
                    } else {
                        apiCaseSerialService.serial(executionQueue);
                    }
                }
            } else {
                // 集合报告合并
                this.margeReport(dto);
                queueMapper.deleteByPrimaryKey(dto.getQueueId());
                LoggerUtil.info("Queue execution ends：" + dto.getQueueId());
            }

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            executionQueueDetailMapper.deleteByExample(example);
        }
        LoggerUtil.info("处理队列结束：" + dto.getReportId() + "QID：" + dto.getQueueId());
    }

    private void margeReport(ResultDTO dto) {
        if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            String reportId = dto.getReportId();
            if (StringUtils.equalsIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name())) {
                reportId = dto.getTestPlanReportId();
            }
            apiScenarioReportService.margeReport(reportId, dto.getRunMode(), dto.getConsole());
        }
    }

    public void defendQueue() {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        // 计算一小时前的超时报告
        final long timeout = System.currentTimeMillis() - (60 * MINUTE_MILLIS);
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
            if (StringUtils.isNotEmpty(queue.getPoolId()) && jMeterService.getRunningQueue(queue.getPoolId(), item.getReportId())) {
                continue;
            }
            // 检查执行报告是否还在等待队列中或执行线程中
            if (JMeterThreadUtils.isRunning(item.getReportId(), item.getTestId())) {
                continue;
            }
            // 检查是否已经超时
            ResultDTO dto = new ResultDTO();
            dto.setQueueId(item.getQueueId());
            dto.setTestId(item.getTestId());
            if (StringUtils.equalsAnyIgnoreCase(queue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                redisTemplateService.delete(JmxFileUtil.getExecuteFileKeyInRedis(item.getReportId()));
                ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                // 报告已经被删除则队列也删除
                if (report == null) {
                    executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                }
                // 这里只处理已经开始执行的队列如果 报告状态是 Waiting 表示还没开始暂不处理
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), TestPlanReportStatus.RUNNING.name()) && report.getUpdateTime() < timeout) {
                    report.setStatus(ApiReportStatus.ERROR.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                    LoggerUtil.info("超时处理报告：" + report.getId());
                    if (queue != null && StringUtils.equalsIgnoreCase(item.getType(), RunModeConstants.SERIAL.toString())) {
                        // 删除串行资源锁
                        String key = StringUtils.join(RunModeConstants.SERIAL.name(), "_", dto.getReportId());
                        redisTemplateService.delete(key);

                        LoggerUtil.info("超时处理报告：【" + report.getId() + "】进入下一个执行");
                        dto.setTestPlanReportId(queue.getReportId());
                        dto.setReportId(queue.getReportId());
                        dto.setRunMode(queue.getRunMode());
                        dto.setRunType(item.getType());
                        dto.setReportType(queue.getReportType());
                        queueNext(dto);
                    } else {
                        executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                    }
                }
            } else {
                redisTemplateService.delete(JmxFileUtil.getExecuteFileKeyInRedis(item.getReportId()));
                // 用例/接口超时结果处理
                ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(item.getReportId());
                if (result != null && StringUtils.equalsAnyIgnoreCase(result.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                    result.setStatus(ApiReportStatus.ERROR.name());
                    apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                    executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                    dto.setTestPlanReportId(queue.getReportId());
                    dto.setReportId(queue.getReportId());
                    dto.setRunMode(queue.getRunMode());
                    dto.setRunType(item.getType());
                    dto.setReportType(queue.getReportType());
                    queueNext(dto);
                }
            }
        }
        // 集成报告超时处理
        ApiExecutionQueueExample queueDetailExample = new ApiExecutionQueueExample();
        queueDetailExample.createCriteria().andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString()).andCreateTimeLessThan(timeout);
        List<ApiExecutionQueue> executionQueues = queueMapper.selectByExample(queueDetailExample);
        if (CollectionUtils.isNotEmpty(executionQueues)) {
            executionQueues.forEach(item -> {
                ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(),
                        TestPlanReportStatus.RUNNING.name(), ApiReportStatus.PENDING.name()) && (report.getUpdateTime() < timeout)) {
                    report.setStatus(ApiReportStatus.ERROR.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                    if (FileServer.getFileServer() != null) {
                        FileServer.getFileServer().closeCsv(item.getReportId());
                    }
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
                    checkTestPlanCaseTestEnd(null, null, item.getReportId());
                }
            });
        }

        List<String> testPlanReports = extApiExecutionQueueMapper.findTestPlanRunningReport();
        if (CollectionUtils.isNotEmpty(testPlanReports)) {
            testPlanReports.forEach(reportId -> {
                LoggerUtil.info("Compensation Test Plan Report：【" + reportId + "】");
                checkTestPlanCaseTestEnd(null, null, reportId);
            });
        }
        // 清除异常队列/一般是服务突然停止产生
        extApiExecutionQueueMapper.delete();
    }

    public void stop(String reportId) {
        if (FileServer.getFileServer() != null) {
            FileServer.getFileServer().closeCsv(reportId);
        }
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
                    checkTestPlanCaseTestEnd(null, null, queue.getReportId());
                }
            }
        });
    }

    public void stop(List<String> reportIds) {
        if (CollectionUtils.isEmpty(reportIds)) {
            return;
        }
        // 清理CSV
        reportIds.forEach(item -> {
            if (FileServer.getFileServer() != null) {
                FileServer.getFileServer().closeCsv(item);
            }
        });
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
                checkTestPlanCaseTestEnd(null, null, queue.getReportId());
                queueMapper.deleteByPrimaryKey(queueId);
            }
        }
    }

    /**
     * 服务异常重启处理
     */
    public void exceptionHandling() {
        LogUtil.info("开始处理服务重启导致执行未完成的报告状态");
        // 更新报告状态
        extApiScenarioReportMapper.updateAllStatus();
        // 更新用例报告状态
        extApiDefinitionExecResultMapper.updateAllStatus();
        LogUtil.info("处理服务重启导致执行未完成的报告状态完成");
    }
}
