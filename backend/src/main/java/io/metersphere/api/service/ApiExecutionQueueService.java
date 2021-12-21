package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunModeDataDTO;
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
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public DBTestQueue add(Object runObj, String poolId, String type, String reportId, String reportType, String runMode) {
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

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiExecutionQueueDetailMapper batchMapper = sqlSession.getMapper(ApiExecutionQueueDetailMapper.class);
        if (StringUtils.equals(type, ApiRunMode.API_PLAN.name())) {
            final int[] sort = {0};
            if (StringUtils.equals(reportType, RunModeConstants.PARALLEL.toString())) {
                Map<String, TestPlanApiCase> runMap = (Map<String, TestPlanApiCase>) runObj;
                runMap.forEach((k, v) -> {
                    ApiExecutionQueueDetail queue = detail(k, v.getId(), type, sort[0], executionQueue.getId(), null);
                    if (sort[0] == 0) {
                        resQueue.setQueue(queue);
                    }
                    sort[0]++;
                    batchMapper.insert(queue);
                });
            } else {
                Map<TestPlanApiCase, ApiDefinitionExecResult> runMap = (Map<TestPlanApiCase, ApiDefinitionExecResult>) runObj;
                runMap.forEach((k, v) -> {
                    ApiExecutionQueueDetail queue = detail(v.getId(), k.getId(), type, sort[0], executionQueue.getId(), null);
                    if (sort[0] == 0) {
                        resQueue.setQueue(queue);
                    }
                    sort[0]++;
                    batchMapper.insert(queue);
                });
            }

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
            });
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
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
                List<ApiExecutionQueueDetail> queues = executionQueueDetailMapper.selectByExample(example);
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
        long now = System.currentTimeMillis();
        // 八分钟前的数据
        now = now - 8 * MINUTE_MILLIS;
        ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
        example.createCriteria().andCreateTimeLessThan(now);
        List<ApiExecutionQueueDetail> queueDetails = executionQueueDetailMapper.selectByExample(example);

        if (CollectionUtils.isNotEmpty(queueDetails)) {
            queueDetails.forEach(item -> {
                if (StringUtils.equalsAny(item.getType(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                    ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(item.getReportId());
                    if (report != null && StringUtils.equalsAny(report.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                        report.setStatus("timeout");
                        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                    }
                } else {
                    ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(item.getReportId());
                    if (result != null && StringUtils.equalsAny(result.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                        result.setStatus("timeout");
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
                if (report != null && StringUtils.equalsAny(report.getStatus(), TestPlanReportStatus.RUNNING.name())) {
                    report.setStatus("timeout");
                    apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                }
            });
        }
        // 清除异常队列/一般是服务突然停止产生
        extApiExecutionQueueMapper.delete();
    }
}
