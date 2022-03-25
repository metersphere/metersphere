package io.metersphere.task.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.automation.TaskRequest;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.jmeter.JMeterService;
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
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }

    public String stop(List<TaskRequest> reportIds) {
        if (CollectionUtils.isNotEmpty(reportIds)) {
            // 聚类，同一批资源池的一批发送
            Map<String, List<String>> poolMap = new HashMap<>();
            for (TaskRequest request : reportIds) {
                String actuator = null;
                if (StringUtils.isNotEmpty(request.getReportId())) {
                    // 从队列移除
                    execThreadPoolExecutor.removeQueue(request.getReportId());
                    apiExecutionQueueService.stop(request.getReportId());
                    PoolExecBlockingQueueUtil.offer(request.getReportId());
                    if (StringUtils.equals(request.getType(), "API")) {
                        ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                        if (result != null) {
                            result.setStatus("STOP");
                            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                            actuator = result.getActuator();
                        }
                    } else if (StringUtils.equals(request.getType(), "SCENARIO")) {
                        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(request.getReportId());
                        if (report != null) {
                            report.setStatus("STOP");
                            apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                            actuator = report.getActuator();
                        }
                    } else if (StringUtils.equals(request.getType(), "PERFORMANCE")) {
                        performanceTestService.stopTest(request.getReportId(), false);
                    }
                    extracted(poolMap, request, actuator);
                } else {
                    if (StringUtils.equals(request.getType(), "API")) {
                        List<ApiDefinitionExecResult> result = extApiDefinitionExecResultMapper.selectApiResultByProjectId(request.getProjectId());
                        if (CollectionUtils.isNotEmpty(result)) {
                            for (ApiDefinitionExecResult item : result) {
                                item.setStatus("STOP");
                                apiDefinitionExecResultMapper.updateByPrimaryKeySelective(item);
                                actuator = item.getActuator();
                                request.setReportId(item.getId());
                                extracted(poolMap, request, actuator);
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(item.getId());
                                apiExecutionQueueService.stop(item.getId());
                                PoolExecBlockingQueueUtil.offer(item.getId());
                            }
                        }
                    } else if (StringUtils.equals(request.getType(), "SCENARIO")) {
                        List<ApiScenarioReport> reports = extApiScenarioReportMapper.selectReportByProjectId(request.getProjectId());
                        if (CollectionUtils.isNotEmpty(reports)) {
                            for (ApiScenarioReport report : reports) {
                                report.setStatus("STOP");
                                apiScenarioReportMapper.updateByPrimaryKeySelective(report);
                                actuator = report.getActuator();
                                request.setReportId(report.getId());
                                extracted(poolMap, request, actuator);
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(report.getId());
                                apiExecutionQueueService.stop(report.getId());
                                PoolExecBlockingQueueUtil.offer(report.getId());
                            }
                        }
                    } else if (StringUtils.equals(request.getType(), "PERFORMANCE")) {
                        List<LoadTestReport> loadTestReports = extLoadTestReportMapper.selectReportByProjectId(request.getProjectId());
                        if (CollectionUtils.isNotEmpty(loadTestReports)) {
                            for (LoadTestReport loadTestReport : loadTestReports) {
                                performanceTestService.stopTest(loadTestReport.getId(), false);
                                request.setReportId(loadTestReport.getId());
                                extracted(poolMap, request, actuator);
                                // 从队列移除
                                execThreadPoolExecutor.removeQueue(loadTestReport.getId());
                                apiExecutionQueueService.stop(loadTestReport.getId());
                                PoolExecBlockingQueueUtil.offer(loadTestReport.getId());
                            }
                        }
                    }
                }
                if (!poolMap.isEmpty()) {
                    this.send(poolMap);
                }
            }
        }
        return "SUCCESS";
    }

    private void extracted(Map<String, List<String>> poolMap, TaskRequest request, String actuator) {
        if (StringUtils.isNotEmpty(actuator) && !StringUtils.equals(actuator, "LOCAL")) {
            if (poolMap.containsKey(actuator)) {
                poolMap.get(actuator).add(request.getReportId());
            } else {
                poolMap.put(actuator, new ArrayList<String>() {{
                    this.add(request.getReportId());
                }});
            }
        } else {
            new LocalRunner().stop(request.getReportId());
        }
    }
}
