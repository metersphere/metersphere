package io.metersphere.consul;


import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.request.QueryTestPlanRequest;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.service.PerformanceTestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class ConsulService {
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;
    @Resource
    private PerformanceTestService performanceTestService;

    public Map<String, List<String>> getActiveNodes() {
        if (cache.size() == 0) {
            updateCache();
        }
        return cache;
    }

    public void updateCache() {
        Map<String, List<String>> result = new HashMap<>();
// todo 资源池
        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        List<TestResourcePoolDTO> testResourcePoolDTOS = baseTestResourcePoolService.listResourcePools(resourcePoolRequest);
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        List<LoadTestDTO> list = performanceTestService.list(request);
        for (LoadTestDTO loadTestDTO : list) {
            String advancedConfiguration = performanceTestService.getAdvancedConfiguration(loadTestDTO.getId());
            Map adv = JSON.parseObject(advancedConfiguration, Map.class);
            Object o1 = adv.get("monitorParams");
            if (o1 == null) {
                continue;
            }

            List monitorParams = (List) adv.get("monitorParams");

            for (Object monitorParam : monitorParams) {
                Map o = (Map) monitorParam;
                result.put(o.get("ip") + "-" + o.get("port"), Collections.singletonList("metersphere"));
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
                // node-controller 监控端口 8082
                result.put(node.getIp() + "-" + node.getPort(), Collections.singletonList("metersphere"));
            }
        }
        cache.clear();
        cache.putAll(result);
    }
}
