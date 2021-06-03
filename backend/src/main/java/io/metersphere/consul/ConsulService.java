package io.metersphere.consul;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.performance.dto.Monitor;
import io.metersphere.performance.request.QueryTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ConsulService {
    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private PerformanceTestService performanceTestService;

    public Map<String, List<String>> getActiveNodes() {
        Map<String, List<String>> result = new HashMap<>();

        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        List<TestResourcePoolDTO> testResourcePoolDTOS = testResourcePoolService.listResourcePools(resourcePoolRequest);
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setFilters(new HashMap<String, List<String>>() {{
            put("status", Arrays.asList(PerformanceTestStatus.Starting.name(), PerformanceTestStatus.Running.name()));
        }});
        List<LoadTestDTO> list = performanceTestService.list(request);
        for (LoadTestDTO loadTestDTO : list) {
            String advancedConfiguration = performanceTestService.getAdvancedConfiguration(loadTestDTO.getId());
            JSONObject adv = JSON.parseObject(advancedConfiguration);
            Object monitorParams = adv.get("monitorParams");
            if (monitorParams == null) {
                continue;
            }
            List<Monitor> monitors = JSON.parseArray(monitorParams.toString(), Monitor.class);
            for (Monitor monitor : monitors) {
                result.put(monitor.getIp() + "-" + monitor.getPort(), Collections.singletonList("metersphere"));
            }
        }
        for (TestResourcePoolDTO pool : testResourcePoolDTOS) {
            if (!StringUtils.equals(pool.getType(), ResourcePoolTypeEnum.NODE.name())) {
                continue;
            }
            List<TestResource> resources = pool.getResources();
            for (TestResource resource : resources) {
                NodeDTO node = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                // 资源池默认9100
                int port = 9100;
                if (node.getMonitorPort() != null) {
                    port = node.getMonitorPort();
                }
                result.put(node.getIp() + "-" + port, Collections.singletonList("metersphere"));
            }
        }
        return result;
    }
}
