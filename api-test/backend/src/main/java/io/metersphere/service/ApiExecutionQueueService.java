package io.metersphere.service;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.exec.api.ApiCaseSerialService;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseApiExecutionQueueMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.scenario.ApiScenarioReportService;
import io.metersphere.utils.JsonUtils;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.ResultVO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class ApiExecutionQueueService {
    @Resource
    protected ApiExecutionQueueMapper queueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
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
    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode, RunModeConfigDTO config) {
        LoggerUtil.info("报告【" + type + "】开始生成执行链", reportId);
        if (config.getEnvMap() == null) {
            config.setEnvMap(new LinkedHashMap<>());
        }
        ApiExecutionQueue executionQueue = getApiExecutionQueue(poolId, reportId, reportType, runMode, config);
        queueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);

        Map<String, String> detailMap = new HashMap<>();
        List<ApiExecutionQueueDetail> queueDetails = new LinkedList<>();
        // 初始化API/用例队列
        String redisLockType = TestPlanExecuteCaseType.SCENARIO.name();
        if (StringUtils.equalsAnyIgnoreCase(type, ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name())) {
            Map<String, ApiDefinitionExecResult> runMap = (Map<String, ApiDefinitionExecResult>) runObj;
            initApi(runMap, resQueue, config, detailMap, queueDetails);
            redisLockType = TestPlanExecuteCaseType.API_CASE.name();
        }
        // 初始化场景
        else {
            Map<String, RunModeDataDTO> runMap = (Map<String, RunModeDataDTO>) runObj;
            initScenario(runMap, resQueue, config, detailMap, queueDetails);
        }
        if (CollectionUtils.isNotEmpty(queueDetails)) {
            extApiExecutionQueueMapper.sqlInsert(queueDetails);
        }
        //redis移除key （执行测试计划时会添加key)
        redisTemplateService.unlock(reportId, redisLockType, reportId);
        resQueue.setDetailMap(detailMap);
        LoggerUtil.info("报告【" + type + "】生成执行链结束", reportId);
        return resQueue;
    }

    @Resource
    private RedisTemplateService redisTemplateService;

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
                List<String> kyList = v.getPlanEnvMap().keySet().stream().toList();
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
        LoggerUtil.info("进入失败停止处理：" + executionQueue.getId());
        boolean isError = false;
        String status = null;
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(CommonConstants.LOCAL_STATUS_KEY)) {
            ResultVO resultVO = JsonUtils.convertValue(dto.getArbitraryData().get(CommonConstants.LOCAL_STATUS_KEY), ResultVO.class);
            if (ObjectUtils.isNotEmpty(resultVO)) {
                status = resultVO.getStatus();
            }
            if (StringUtils.equalsIgnoreCase(status, ApiReportStatus.ERROR.name())) {
                isError = true;
            }
        }
        if (StringUtils.isBlank(status)) {
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
        }
        if (isError) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId());
            checkTestPlanCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
            // 更新未执行的报告状态
            List<ApiExecutionQueueDetail> details = executionQueueDetailMapper.selectByExample(example);
            List<String> reportIds = details.stream().map(ApiExecutionQueueDetail::getReportId).collect(toList());
            List<String> testIds = details.stream().map(ApiExecutionQueueDetail::getTestId).collect(toList());
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
        if (executionQueue == null) {
            LoggerUtil.info("The queue was accidentally deleted：【" + id + "】");
            return queue;
        }
        BeanUtils.copyBean(queue, executionQueue);
        LoggerUtil.info("Get the next execution point：【" + id + "】");

        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.setOrderByClause("sort asc");
        example.createCriteria().andQueueIdEqualTo(id);
        List<ApiExecutionQueueDetail> queues = executionQueueDetailMapper.selectByExampleWithBLOBs(example);

        if (CollectionUtils.isNotEmpty(queues)) {
            // 处理掉当前已经执行完成的资源
            List<ApiExecutionQueueDetail> completedQueues = queues.stream()
                    .filter(item -> StringUtils.equals(item.getTestId(), testId))
                    .collect(toList());
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
                List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(toList());
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
        // 清理当前节点并获取下一个要执行的资源
        DBTestQueue executionQueue = this.handleQueue(dto.getQueueId(), dto.getTestId());
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
                if (StringUtils.equalsAny(executionQueue.getRunMode(),
                        ApiRunMode.SCENARIO.name(),
                        ApiRunMode.SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO.name(),
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                    // 场景执行
                    apiScenarioSerialService.serial(executionQueue);
                } else {
                    // 接口用例执行
                    apiCaseSerialService.serial(executionQueue);
                }
            }
        } else {
            // 集合报告合并
            this.margeReport(dto);
            queueMapper.deleteByPrimaryKey(dto.getQueueId());
            LoggerUtil.info("Queue execution ends：" + dto.getQueueId());
        }
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


    private void subitemHandling(List<ApiExecutionQueueDetail> queueDetails, ApiExecutionQueue queue, long timeout) {
        for (ApiExecutionQueueDetail item : queueDetails) {
            if (queue == null) {
                queue = queueMapper.selectByPrimaryKey(item.getQueueId());
            }
            if (queue == null) {
                executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                continue;
            }

            String reportId = StringUtils.equalsIgnoreCase(queue.getReportType(), RunModeConstants.SET_REPORT.toString())
                    ? StringUtils.join(queue.getReportId(), "_", item.getTestId())
                    : item.getReportId();

            // 在资源池中执行
            if (StringUtils.isNotEmpty(queue.getPoolId()) && jMeterService.getRunningQueue(queue.getPoolId(), reportId)) {
                continue;
            }
            // 检查是否已经超时
            ResultDTO dto = new ResultDTO();
            dto.setQueueId(item.getQueueId());
            dto.setTestId(item.getTestId());

            if (StringUtils.equalsAnyIgnoreCase(queue.getRunMode(),
                    ApiRunMode.SCENARIO.name(),
                    ApiRunMode.SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO.name(),
                    ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {

                ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                // 报告已经被删除则队列也删除
                if (report == null) {
                    executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
                }
                // 这里只处理已经开始执行的队列如果 报告状态是 Waiting 表示还没开始暂不处理
                if (report != null && StringUtils.equalsAnyIgnoreCase(report.getStatus(), ApiReportStatus.RUNNING.name()) && report.getUpdateTime() < timeout) {
                    report.setStatus(ApiReportStatus.ERROR.name());
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);

                    LoggerUtil.info("超时场景报告处理：{}", report.getId());
                    integrateData(queue, item, dto);
                }
            } else {
                // 用例/接口超时结果处理
                ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(item.getReportId());
                if (result != null && StringUtils.equalsAnyIgnoreCase(result.getStatus(), ApiReportStatus.RUNNING.name()) && result.getStartTime() < timeout) {
                    result.setStatus(ApiReportStatus.ERROR.name());
                    apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);

                    LoggerUtil.info("超时API报告处理：{}", item.getReportId());

                    integrateData(queue, item, dto);
                }
            }
        }
    }

    private void integrateData(ApiExecutionQueue queue, ApiExecutionQueueDetail item, ResultDTO dto) {
        if (StringUtils.equalsIgnoreCase(item.getType(), RunModeConstants.SERIAL.toString())) {
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

    public void defendQueue() {
        // 计算一小时前的超时报告
        final long timeout = System.currentTimeMillis() - 60 * 60 * 1000;

        // 集成报告超时处理
        ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
        queueExample.createCriteria()
                .andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString())
                .andCreateTimeLessThan(timeout);

        List<ApiExecutionQueue> executionQueues = queueMapper.selectByExample(queueExample);

        if (CollectionUtils.isNotEmpty(executionQueues)) {
            List<String> queueIds = executionQueues.stream()
                    .map(ApiExecutionQueue::getId)
                    .collect(toList());

            ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
            detailExample.createCriteria().andQueueIdIn(queueIds);

            Map<String, List<ApiExecutionQueueDetail>> detailMap = executionQueueDetailMapper.selectByExample(detailExample)
                    .stream()
                    .collect(groupingBy(ApiExecutionQueueDetail::getQueueId));

            executionQueues.forEach(queue -> {
                List<ApiExecutionQueueDetail> details = detailMap.get(queue.getId());
                if (CollectionUtils.isNotEmpty(details)) {
                    subitemHandling(details, queue, timeout);
                }
            });
        }

        // 独立报告超时处理
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andCreateTimeLessThan(timeout).andTypeNotEqualTo("loadTest");
        List<ApiExecutionQueueDetail> queueDetails = executionQueueDetailMapper.selectByExample(example);

        if (CollectionUtils.isNotEmpty(queueDetails)) {
            subitemHandling(queueDetails, null, timeout);
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

       /* List<String> testPlanReports = extApiExecutionQueueMapper.findTestPlanRunningReport();
        if (CollectionUtils.isNotEmpty(testPlanReports)) {
            testPlanReports.forEach(reportId -> {
                LoggerUtil.info("Compensation Test Plan Report：【" + reportId + "】");
                checkTestPlanCaseTestEnd(null, null, reportId);
            });
        }*/
    }

    public void clearSetReportQueue(String reportId) {
        ApiExecutionQueueExample example = new ApiExecutionQueueExample();
        example.createCriteria()
                .andReportIdEqualTo(reportId)
                .andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString());

        List<ApiExecutionQueue> queues = apiExecutionQueueMapper.selectByExample(example);
        queues.forEach(queue -> {
            apiExecutionQueueMapper.deleteByPrimaryKey(queue.getId());

            ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
            queueDetailExample.createCriteria().andQueueIdEqualTo(queue.getId());
            executionQueueDetailMapper.deleteByExample(queueDetailExample);
        });
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
                    checkTestPlanCaseTestEnd(null, null, queue.getReportId());
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
                checkTestPlanCaseTestEnd(null, null, queue.getReportId());
                queueMapper.deleteByPrimaryKey(queueId);
            }
        }
    }

    /**
     * 服务异常重启处理
     */
    public void exceptionHandling() {
        try {
            LogUtil.info("开始处理服务重启导致执行未完成的报告状态");
            // 更新报告状态
            extApiScenarioReportMapper.updateAllStatus();
            // 更新用例报告状态
            extApiDefinitionExecResultMapper.updateAllStatus();
            LogUtil.info("处理服务重启导致执行未完成的报告状态完成");
            // 清理队列
            final List<String> apiModes = new ArrayList<>() {{
                this.add(ApiRunMode.SCENARIO.name());
                this.add(ApiRunMode.SCHEDULE_SCENARIO.name());
                this.add(ApiRunMode.SCENARIO_PLAN.name());
                this.add(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
                this.add(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
                this.add(ApiRunMode.DEFINITION.name());
                this.add(ApiRunMode.JENKINS.name());
                this.add(ApiRunMode.API_PLAN.name());
                this.add(ApiRunMode.SCHEDULE_API_PLAN.name());
                this.add(ApiRunMode.JENKINS_API_PLAN.name());
                this.add(ApiRunMode.MANUAL_PLAN.name());
            }};

            ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
            queueExample.createCriteria().andRunModeIn(apiModes);
            List<ApiExecutionQueue> queues = apiExecutionQueueMapper.selectByExample(queueExample);
            if (CollectionUtils.isNotEmpty(queues)) {
                List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(toList());
                ApiExecutionQueueDetailExample queueDetailExample = new ApiExecutionQueueDetailExample();
                queueDetailExample.createCriteria().andQueueIdIn(ids);
                apiExecutionQueueDetailMapper.deleteByExample(queueDetailExample);
                // 通知测试计划
                queues.forEach(item -> {
                    if (StringUtils.endsWith(item.getRunMode(), "PLAN")) {
                        checkTestPlanCaseTestEnd(null, null, item.getReportId());
                    }
                });
                apiExecutionQueueMapper.deleteByExample(queueExample);
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
    }
}
