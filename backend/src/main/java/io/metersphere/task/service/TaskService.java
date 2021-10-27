package io.metersphere.task.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.automation.TaskRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.LocalRunner;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.base.mapper.ext.ExtTaskMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.NodeDTO;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<TaskCenterDTO> getTasks(TaskCenterRequest request) {
        if (StringUtils.isEmpty(request.getProjectId())) {
            return new ArrayList<>();
        }
        return extTaskMapper.getTasks(request);
    }

    public int getRunningTasks(TaskCenterRequest request) {
        if (StringUtils.isEmpty(request.getProjectId())) {
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
                if (StringUtils.equals(request.getType(), "API")) {
                    ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                    if (result != null) {
                        result.setStatus("STOP");
                        apiDefinitionExecResultMapper.updateByPrimaryKeySelective(result);
                        actuator = result.getActuator();
                        MessageCache.batchTestCases.remove(result.getId());
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
                MessageCache.cache.remove(request.getReportId());
                MessageCache.terminationOrderDeque.add(request.getReportId());
            }
            if (!poolMap.isEmpty()) {
                this.send(poolMap);
            }
        }
        return "SUCCESS";
    }
}
