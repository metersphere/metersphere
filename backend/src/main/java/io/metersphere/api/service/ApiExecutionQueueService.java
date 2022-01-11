package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.ScenarioStatus;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtApiExecutionQueueMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiExecutionQueueService {
    @Resource
    private ApiExecutionQueueMapper queueMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
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
    private ExtApiExecutionQueueMapper extApiExecutionQueueMapper;

    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode, Map<String, String> envMap) {
        ApiExecutionQueue executionQueue = new ApiExecutionQueue();
        executionQueue.setId(UUID.randomUUID().toString());
        executionQueue.setCreateTime(System.currentTimeMillis());
        executionQueue.setPoolId(poolId);
        executionQueue.setReportId(reportId);
        executionQueue.setReportType(StringUtils.isNotEmpty(reportType) ? reportType : RunModeConstants.INDEPENDENCE.toString());
        executionQueue.setRunMode(runMode);
        queueMapper.insert(executionQueue);
        DBTestQueue resQueue = new DBTestQueue();
        BeanUtils.copyBean(resQueue, executionQueue);
        Map<String, String> detailMap = new HashMap<>();

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiExecutionQueueDetailMapper batchMapper = sqlSession.getMapper(ApiExecutionQueueDetailMapper.class);
        if (StringUtils.equalsAny(type, ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name())) {
            final int[] sort = {0};
            Map<String, ApiDefinitionExecResult> runMap = (Map<String, ApiDefinitionExecResult>) runObj;
            if (envMap == null) {
                envMap = new LinkedHashMap<>();
            }
            String envStr = JSON.toJSONString(envMap);
            runMap.forEach((k, v) -> {
                ApiExecutionQueueDetail queue = detail(v.getId(), k, type, sort[0], executionQueue.getId(), envStr);
                if (sort[0] == 0) {
                    resQueue.setQueue(queue);
                }
                sort[0]++;
                batchMapper.insert(queue);
                detailMap.put(k, queue.getId());
            });
        } else {
            Map<String, RunModeDataDTO> runMap = (Map<String, RunModeDataDTO>) runObj;
            final int[] sort = {0};
            runMap.forEach((k, v) -> {
                ApiExecutionQueueDetail queue = detail(k, v.getTestId(), type, sort[0], executionQueue.getId(), JSON.toJSONString(v.getPlanEnvMap()));
                queue.setSort(sort[0]);
                if (sort[0] == 0) {
                    resQueue.setQueue(queue);
                }
                sort[0]++;
                batchMapper.insert(queue);
                detailMap.put(k, queue.getId());
            });
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        resQueue.setDetailMap(detailMap);
        return resQueue;
    }

    private ApiExecutionQueueDetail detail(String reportId, String testId, String type, int sort, String queueId, String envMap) {
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

    public DBTestQueue edit(String id, String testId) {
        ApiExecutionQueue executionQueue = queueMapper.selectByPrimaryKey(id);
        if (executionQueue != null) {
            DBTestQueue queue = new DBTestQueue();
            BeanUtils.copyBean(queue, executionQueue);
            if (executionQueue != null) {
                ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
                example.setOrderByClause("sort asc");
                example.createCriteria().andQueueIdEqualTo(id);
                List<ApiExecutionQueueDetail> queues = executionQueueDetailMapper.selectByExampleWithBLOBs(example);
                if (CollectionUtils.isNotEmpty(queues)) {
                    List<ApiExecutionQueueDetail> list = queues.stream().filter(item -> StringUtils.equals(item.getTestId(), testId)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(list)) {
                        executionQueueDetailMapper.deleteByPrimaryKey(list.get(0).getId());
                        queues.remove(list.get(0));
                        BeanUtils.copyBean(queue, executionQueue);
                        if (CollectionUtils.isNotEmpty(queues)) {
                            queue.setQueue(queues.get(0));
                        }
                    }
                    if (CollectionUtils.isEmpty(queues)) {
                        queueMapper.deleteByPrimaryKey(id);
                    }
                } else {
                    queueMapper.deleteByPrimaryKey(id);
                }
            }
            return queue;
        }
        return null;
    }

    public void queueNext(ResultDTO dto) {
        DBTestQueue executionQueue = this.edit(dto.getQueueId(), dto.getTestId());
        if (executionQueue != null) {
            LoggerUtil.info("开始处理执行队列：" + executionQueue.getId());
            if (executionQueue.getQueue() != null && StringUtils.isNotEmpty(executionQueue.getQueue().getTestId())) {
                if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
                    LoggerUtil.info("当前执行队列是：" + JSON.toJSONString(executionQueue.getQueue()));
                    apiScenarioSerialService.serial(executionQueue, executionQueue.getQueue());
                }
            } else {
                if (StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    apiScenarioReportService.margeReport(dto.getReportId());
                }
                // 更新测试计划报告
                if (StringUtils.isNotEmpty(dto.getTestPlanReportId())) {
                    CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(dto.getTestPlanReportId(), TestPlanReportStatus.COMPLETED.name());
                }
                queueMapper.deleteByPrimaryKey(executionQueue.getId());
                LoggerUtil.info("队列：" + dto.getQueueId() + " 执行结束");
            }

            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andQueueIdEqualTo(dto.getQueueId()).andTestIdEqualTo(dto.getTestId());
            executionQueueDetailMapper.deleteByExample(example);
        }
    }


    public void timeOut() {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        // 二十分钟前的超时报告
        final long now = System.currentTimeMillis() - (20 * MINUTE_MILLIS);
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andCreateTimeLessThan(now);
        List<ApiExecutionQueueDetail> queueDetails = executionQueueDetailMapper.selectByExample(example);

        if (CollectionUtils.isNotEmpty(queueDetails)) {
            queueDetails.forEach(item -> {
                if (StringUtils.equalsAny(item.getType(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                    ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                    if (report != null && StringUtils.equalsAny(report.getStatus(), TestPlanReportStatus.RUNNING.name()) && report.getUpdateTime() < now) {
                        report.setStatus(ScenarioStatus.Timeout.name());
                        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                    }
                } else {
                    ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(item.getReportId());
                    if (result != null && StringUtils.equalsAny(result.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                        result.setStatus(ScenarioStatus.Timeout.name());
                        apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                    }
                }
                executionQueueDetailMapper.deleteByPrimaryKey(item.getId());
            });
        }

        ApiExecutionQueueExample queueDetailExample = new ApiExecutionQueueExample();
        queueDetailExample.createCriteria().andReportTypeEqualTo(RunModeConstants.SET_REPORT.toString()).andCreateTimeLessThan(now);
        List<ApiExecutionQueue> executionQueues = queueMapper.selectByExample(queueDetailExample);
        if (CollectionUtils.isNotEmpty(executionQueues)) {
            executionQueues.forEach(item -> {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                if (report != null && StringUtils.equalsAny(report.getStatus(), TestPlanReportStatus.RUNNING.name()) && report.getUpdateTime() < now) {
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
                    CommonBeanFactory.getBean(TestPlanReportService.class).finishedTestPlanReport(item.getReportId(), TestPlanReportStatus.COMPLETED.name());
                }
            });
        }
        // 清除异常队列/一般是服务突然停止产生
        extApiExecutionQueueMapper.delete();
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
}
