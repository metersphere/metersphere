package io.metersphere.task.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.automation.TaskRequest;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.JMeterThreadUtils;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtTaskMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.NodeDTO;
import io.metersphere.jmeter.LocalRunner;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {
    @Resource
    private ExtTaskMapper extTaskMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private PerformanceTestService performanceTestService;
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
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private ExecThreadPoolExecutor execThreadPoolExecutor;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private CheckPermissionService checkPermissionService;

    public List<String> getOwnerProjectIds(String userId) {
        Set<String> userRelatedProjectIds = null;
        if (StringUtils.isEmpty(userId)) {
            userRelatedProjectIds = checkPermissionService.getUserRelatedProjectIds();
        } else {
            userRelatedProjectIds = checkPermissionService.getOwnerByUserId(userId);
        }
        if (CollectionUtils.isEmpty(userRelatedProjectIds)) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(userRelatedProjectIds);
    }

    public List<TaskCenterDTO> getTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return extTaskMapper.getTasks(request);
    }

    public int getRunningTasks(TaskCenterRequest request) {
        request.setProjects(this.getOwnerProjectIds(request.getUserId()));
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return 0;
        }
        return extTaskMapper.getRunningTasks(request);
    }

    public List<TaskCenterDTO> getCases(String id) {
        return extTaskMapper.getCases(id);
    }

    public List<TaskCenterDTO> getScenario(String id) {
        return extTaskMapper.getScenario(id);
    }


    public void send(Map<String, List<String>> poolMap) {
        try {
            LoggerUtil.info("结束所有NODE中执行的资源");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("STOP-NODE");
                    for (String poolId : poolMap.keySet()) {
                        TestResourcePoolExample example = new TestResourcePoolExample();
                        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(poolId);
                        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);
                        if (CollectionUtils.isNotEmpty(pools)) {
                            List<String> poolIds = pools.stream().map(pool -> pool.getId()).collect(Collectors.toList());
                            TestResourceExample resourceExample = new TestResourceExample();
                            resourceExample.createCriteria().andTestResourcePoolIdIn(poolIds);
                            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(resourceExample);
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
                }
            });
            thread.start();
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }

    public String stop(List<TaskRequest> taskRequests) {
        if (CollectionUtils.isNotEmpty(taskRequests)) {
            List<TaskRequest> stopTasks = taskRequests.stream().filter(s -> StringUtils.isNotEmpty(s.getReportId())).collect(Collectors.toList());
            // 聚类，同一批资源池的一批发送
            Map<String, List<String>> poolMap = new HashMap<>();
            // 单条停止
            if (CollectionUtils.isNotEmpty(stopTasks) && stopTasks.size() == 1) {
                // 从队列移除
                TaskRequest request = stopTasks.get(0);
                execThreadPoolExecutor.removeQueue(request.getReportId());
                apiExecutionQueueService.stop(request.getReportId());
                PoolExecBlockingQueueUtil.offer(request.getReportId());
                if (StringUtils.equals(request.getType(), "API")) {
                    ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                    if (result != null) {
                        result.setStatus("STOP");
                        apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                        extracted(poolMap, request.getReportId(), result.getActuator());
                    }
                }
                if (StringUtils.equals(request.getType(), "SCENARIO")) {
                    ApiScenarioReportWithBLOBs report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                    if (report != null) {
                        report.setStatus("STOP");
                        apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                        extracted(poolMap, request.getReportId(), report.getActuator());
                    }
                }
                if (StringUtils.equals(request.getType(), "PERFORMANCE")) {
                    performanceTestService.stopTest(request.getReportId(), false);
                }

            } else {
                try {
                    LoggerUtil.info("进入批量停止方法");
                    // 全部停止
                    Map<String, TaskRequest> taskRequestMap = taskRequests.stream().collect(Collectors.toMap(TaskRequest::getType, taskRequest -> taskRequest));
                    // 获取工作空间项目
                    LoggerUtil.info("获取工作空间对应的项目");
                    TaskCenterRequest taskCenterRequest = new TaskCenterRequest();
                    taskCenterRequest.setProjects(this.getOwnerProjectIds(taskRequestMap.get("SCENARIO").getUserId()));

                    // 结束掉未分发完成的任务
                    LoggerUtil.info("结束正在进行中的计划任务队列");
                    JMeterThreadUtils.stop("PLAN-CASE");
                    JMeterThreadUtils.stop("API-CASE-RUN");
                    JMeterThreadUtils.stop("SCENARIO-PARALLEL-THREAD");

                    if (taskRequestMap.containsKey("API")) {
                        List<ApiDefinitionExecResult> results = extApiDefinitionExecResultMapper.findByProjectIds(taskCenterRequest);
                        LoggerUtil.info("查询API进行中的报告：" + results.size());
                        if (CollectionUtils.isNotEmpty(results)) {
                            for (ApiDefinitionExecResult item : results) {
                                extracted(poolMap, item.getId(), item.getActuator());
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(item.getId());
                                PoolExecBlockingQueueUtil.offer(item.getId());
                            }
                            LoggerUtil.info("结束API进行中的报告");
                            extTaskMapper.stopApi(taskCenterRequest);
                            // 清理队列并停止测试计划报告
                            LoggerUtil.info("清理API执行链");
                            List<String> ids = results.stream().map(ApiDefinitionExecResult::getId).collect(Collectors.toList());
                            apiExecutionQueueService.stop(ids);
                        }
                    }
                    if (taskRequestMap.containsKey("SCENARIO")) {
                        List<ApiScenarioReport> reports = extApiScenarioReportMapper.findByProjectIds(taskCenterRequest);
                        LoggerUtil.info("查询到执行中的场景报告：" + reports.size());
                        if (CollectionUtils.isNotEmpty(reports)) {
                            for (ApiScenarioReport report : reports) {

                                extracted(poolMap, report.getId(), report.getActuator());
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(report.getId());
                                PoolExecBlockingQueueUtil.offer(report.getId());
                            }

                            // 清理队列并停止测试计划报告
                            LoggerUtil.info("结束所有进行中的场景报告 ");
                            List<String> ids = reports.stream().map(ApiScenarioReport::getId).collect(Collectors.toList());
                            extTaskMapper.stopScenario(taskCenterRequest);
                            // 清理队列并停止测试计划报告
                            LoggerUtil.info("清理队列并停止测试计划报告 ");
                            apiExecutionQueueService.stop(ids);
                        }
                    }
                    if (taskRequestMap.containsKey("PERFORMANCE")) {
                        LoggerUtil.info("开始结束性能测试报告 ");
                        List<LoadTestReport> loadTestReports = extLoadTestReportMapper.selectReportByProjectId(taskRequestMap.get("PERFORMANCE").getProjectId());
                        if (CollectionUtils.isNotEmpty(loadTestReports)) {
                            for (LoadTestReport loadTestReport : loadTestReports) {
                                performanceTestService.stopTest(loadTestReport.getId(), false);
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(loadTestReport.getId());
                                apiExecutionQueueService.stop(loadTestReport.getId());
                                PoolExecBlockingQueueUtil.offer(loadTestReport.getId());
                            }
                        }
                        LoggerUtil.info("结束性能测试报告完成");
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
            if (!poolMap.isEmpty()) {
                this.send(poolMap);
            }
        }
        return "SUCCESS";
    }

    private void extracted(Map<String, List<String>> poolMap, String reportId, String actuator) {
        if (StringUtils.isEmpty(reportId)) {
            return;
        }
        if (StringUtils.isNotEmpty(actuator) && !StringUtils.equals(actuator, "LOCAL")) {
            if (poolMap.containsKey(actuator)) {
                poolMap.get(actuator).add(reportId);
            } else {
                poolMap.put(actuator, new ArrayList<String>() {{
                    this.add(reportId);
                }});
            }
        } else {
            new LocalRunner().stop(reportId);
            JMeterThreadUtils.stop(reportId);
        }
    }
}
