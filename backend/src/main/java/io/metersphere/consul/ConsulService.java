package io.metersphere.consul;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.performance.request.QueryTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ConsulService {
    private static final String RESOURCE_POOL_CACHE_KEY = "RESOURCE_POOL_CACHE";
    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    private static final TypeReference<Map<String, List<String>>> TYPE_REFERENCE = new TypeReference<>() {
    };

    public Map<String, List<String>> getActiveNodes() throws Exception {
        String values = stringRedisTemplate.opsForValue().get(RESOURCE_POOL_CACHE_KEY);
        if (StringUtils.isNotEmpty(values)) {
            return objectMapper.readValue(values, TYPE_REFERENCE);
        }
        return updateCache();
    }

    public Map<String, List<String>> updateCache() throws Exception {
        Map<String, List<String>> result = new HashMap<>();

        QueryResourcePoolRequest resourcePoolRequest = new QueryResourcePoolRequest();
        resourcePoolRequest.setStatus(ResourceStatusEnum.VALID.name());
        List<TestResourcePoolDTO> testResourcePoolDTOS = testResourcePoolService.listResourcePools(resourcePoolRequest);
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        List<LoadTestDTO> list = performanceTestService.list(request);
        for (LoadTestDTO loadTestDTO : list) {
            String advancedConfiguration = performanceTestService.getAdvancedConfiguration(loadTestDTO.getId());
            JSONObject adv = JSON.parseObject(advancedConfiguration);
            Object o1 = adv.get("monitorParams");
            if (o1 == null) {
                continue;
            }

            JSONArray monitorParams = adv.getJSONArray("monitorParams");

            for (int i = 0; i < monitorParams.size(); i++) {
                JSONObject o = monitorParams.getJSONObject(i);
                result.put(o.getString("ip") + "-" + o.getInteger("port"), Collections.singletonList("metersphere"));
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
        stringRedisTemplate.opsForValue().set(RESOURCE_POOL_CACHE_KEY, objectMapper.writeValueAsString(result));
        return result;
    }
}
