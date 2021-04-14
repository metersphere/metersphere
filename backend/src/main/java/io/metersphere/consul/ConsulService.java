package io.metersphere.consul;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.service.TestResourcePoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsulService {
    @Resource
    private TestResourcePoolService testResourcePoolService;

    public Map<String, List<String>> getActiveNodes() {
        List<TestResourcePoolDTO> testResourcePoolDTOS = testResourcePoolService.listValidResourcePools();
        Map<String, List<String>> result = new HashMap<>();
        for (TestResourcePoolDTO pool : testResourcePoolDTOS) {
            if (!StringUtils.equals(pool.getType(), ResourcePoolTypeEnum.NODE.name())) {
                continue;
            }
            List<TestResource> resources = pool.getResources();
            for (TestResource resource : resources) {
                NodeDTO node = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                result.put(node.getIp() + "-9100", Collections.singletonList("metersphere"));
            }
        }
        return result;
    }
}
