package io.metersphere.api.jmeter;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.NodeDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourcePoolCalculation {
    @Resource
    TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    TestResourceMapper testResourceMapper;

    public List<TestResource> getResourcePools(String resourcePoolId) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(resourcePoolId);
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

    public int k8sMaxCoreSize(String resourcePoolId) {
        TestResourceExample resourceExample = new TestResourceExample();
        resourceExample.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId);
        List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(resourceExample);
        if (CollectionUtils.isNotEmpty(testResources)) {
            String configuration = testResources.get(0).getConfiguration();
            if (StringUtils.isNotBlank(configuration)) {
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                return node.getMaxConcurrency();
            }
        }
        return 10;
    }

}
