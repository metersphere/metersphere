package io.metersphere.service.ext;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.base.mapper.ext.BaseTaskMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.StorageEnums;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.vo.TaskResultVO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskRequestDTO;
import io.metersphere.task.service.TaskService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExtApiTaskService extends TaskService {
    @Resource
    private BaseTaskMapper baseTaskMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourceMapper testResourceMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;

    public List<TaskCenterDTO> getCases(String id) {
        return baseTaskMapper.getCases(id);
    }

    public List<TaskCenterDTO> getScenario(String id) {
        return baseTaskMapper.getScenario(id);
    }


    private void send(Map<String, List<String>> poolMap) {
        try {
            LoggerUtil.info("结束所有NODE中执行的资源");
            Map<String, List<TestResource>> process = new HashMap<>();
            for (String poolId : poolMap.keySet()) {
                if (!process.containsKey(poolId)) {
                    List<TestResource> testResources = selectPoolResource(poolId);
                    process.put(poolId, testResources);
                }
                List<TestResource> testResources = process.get(poolId);
                if (CollectionUtils.isNotEmpty(testResources)) {
                    for (TestResource testResource : testResources) {
                        String configuration = testResource.getConfiguration();
                        NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                        String nodeIp = node.getIp();
                        Integer port = node.getPort();
                        String uri = String.format(JMeterService.BASE_URL + "/jmeter/stop", nodeIp, port);
                        restTemplate.postForEntity(uri, poolMap.get(poolId), void.class);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }

    public void apiStop(List<TaskRequestDTO> taskRequests) {
        if (CollectionUtils.isNotEmpty(taskRequests)) {
            List<TaskRequestDTO> stopTasks = taskRequests.stream().filter(s -> StringUtils.isNotEmpty(s.getReportId())).collect(Collectors.toList());
            // 聚类，同一批资源池的一批发送
            Map<String, List<String>> poolMap = new HashMap<>();
            // 单条停止
            if (CollectionUtils.isNotEmpty(stopTasks) && stopTasks.size() == 1) {
                // 从队列移除
                TaskRequestDTO request = stopTasks.get(0);
                apiExecutionQueueService.stop(request.getReportId());
                if (StringUtils.equals(request.getType(), "API")) {
                    ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                    if (result != null) {
                        result.setStatus(ApiReportStatus.STOPPED.name());
                        apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                        extracted(poolMap, request.getReportId(), result.getActuator());
                    }
                }
                if (StringUtils.equals(request.getType(), ElementConstants.SCENARIO_UPPER)) {
                    ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                    if (report != null) {
                        report.setStatus(ApiReportStatus.STOPPED.name());
                        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                        extracted(poolMap, request.getReportId(), report.getActuator());
                    }
                }
                // 开始结束资源池中执行的任务
                if (MapUtils.isNotEmpty(poolMap)) {
                    this.send(poolMap);
                }
            } else {
                Thread thread = new Thread(() -> {
                    this.batchStop(taskRequests);
                });
                thread.start();
            }
        }
    }

    private void batchStop(List<TaskRequestDTO> taskRequests) {
        LoggerUtil.info("进入批量停止方法");
        Map<String, List<String>> poolMap = new HashMap<>();
        // 全部停止
        Map<String, TaskRequestDTO> taskRequestMap = taskRequests.stream().collect(Collectors.toMap(TaskRequestDTO::getType, taskRequest -> taskRequest));
        // 获取工作空间项目
        LoggerUtil.info("获取工作空间对应的项目");
        TaskCenterRequest taskCenterRequest = new TaskCenterRequest();
        taskCenterRequest.setProjects(this.getOwnerProjectIds(taskRequestMap.get(ElementConstants.SCENARIO_UPPER).getUserId()));

        // 结束掉未分发完成的任务
        LoggerUtil.info("结束正在进行中的计划任务队列");
        if (taskRequestMap.containsKey("API")) {
            List<TaskResultVO> results = extApiDefinitionExecResultMapper.findByProjectIds(taskCenterRequest);
            LoggerUtil.info("查询API进行中的报告：" + results.size());
            if (CollectionUtils.isNotEmpty(results)) {
                for (TaskResultVO item : results) {
                    extracted(poolMap, item.getId(), item.getActuator());
                }
                LoggerUtil.info("结束API进行中的报告");
                baseTaskMapper.stopApi(taskCenterRequest);
                // 清理队列并停止测试计划报告
                LoggerUtil.info("清理API执行链");
                List<String> ids = results.stream().map(TaskResultVO::getId).collect(Collectors.toList());
                apiExecutionQueueService.stop(ids);
            }
        }
        if (taskRequestMap.containsKey(ElementConstants.SCENARIO_UPPER)) {
            List<TaskResultVO> reports = extApiScenarioReportMapper.findByProjectIds(taskCenterRequest);
            LoggerUtil.info("查询到执行中的场景报告：" + reports.size());
            if (CollectionUtils.isNotEmpty(reports)) {
                for (TaskResultVO report : reports) {

                    extracted(poolMap, report.getId(), report.getActuator());
                }

                // 清理队列并停止测试计划报告
                LoggerUtil.info("结束所有进行中的场景报告 ");
                List<String> ids = reports.stream().map(TaskResultVO::getId).collect(Collectors.toList());
                baseTaskMapper.stopScenario(taskCenterRequest);
                // 清理队列并停止测试计划报告
                LoggerUtil.info("清理队列并停止测试计划报告 ");
                apiExecutionQueueService.stop(ids);
            }
        }
        // 开始结束资源池中执行的任务
        if (MapUtils.isNotEmpty(poolMap)) {
            this.send(poolMap);
        }
    }

    private void extracted(Map<String, List<String>> poolMap, String reportId, String actuator) {
        if (StringUtils.isEmpty(reportId)) {
            return;
        }
        if (StringUtils.isNotEmpty(actuator) && !StringUtils.equals(actuator, StorageEnums.LOCAL.name())) {
            if (poolMap.containsKey(actuator)) {
                poolMap.get(actuator).add(reportId);
            } else {
                poolMap.put(actuator, new ArrayList<String>() {{
                    this.add(reportId);
                }});
            }
        }
    }

    private List<TestResource> selectPoolResource(String poolId) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(poolId);
        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(pools)) {
            List<String> poolIds = pools.stream().map(TestResourcePool::getId).collect(Collectors.toList());
            TestResourceExample resourceExample = new TestResourceExample();
            resourceExample.createCriteria().andTestResourcePoolIdIn(poolIds);
            resourceExample.setOrderByClause("create_time");
            return testResourceMapper.selectByExampleWithBLOBs(resourceExample);
        }
        return new ArrayList<>();
    }
}
