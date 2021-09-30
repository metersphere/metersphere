package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.JvmInfoDTO;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.dto.NodeDTO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
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
    @Resource
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://%s:%d";

    private JvmInfoDTO getNodeJvmInfo(String uri) {
        try {
            return restTemplate.getForObject(uri, JvmInfoDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public TestResource getPool(String resourcePoolId) {
        // 获取可以执行的资源池
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(resourcePoolId);
        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);
        // 按照NODE节点的可用内存空间大小排序
        List<JvmInfoDTO> availableNodes = new ArrayList<>();
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
                String uri = String.format(BASE_URL + "/jmeter/getJvmInfo", nodeIp, port);
                JvmInfoDTO nodeJvm = this.getNodeJvmInfo(uri);
                if (nodeJvm == null) {
                    continue;
                }
                nodeJvm.setTestResource(testResource);
                availableNodes.add(nodeJvm);
            }
        }
        if (CollectionUtils.isEmpty(availableNodes)) {
            MSException.throwException("未获取到资源池，请检查配置【系统设置-系统-测试资源池】");
        }
        int index = (int) (Math.random() * availableNodes.size());
        JvmInfoDTO jvmInfoDTO = availableNodes.get(index);
        return jvmInfoDTO.getTestResource();
    }


    public List<JvmInfoDTO> getPools(String resourcePoolId) {
        // 获取可以执行的资源池
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andIdEqualTo(resourcePoolId);
        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);

        // 按照NODE节点的可用内存空间大小排序
        List<JvmInfoDTO> availableNodes = new ArrayList<>();
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
                String uri = String.format(BASE_URL + "/jmeter/getJvmInfo", nodeIp, port);
                JvmInfoDTO nodeJvm = this.getNodeJvmInfo(uri);
                if (nodeJvm == null) {
                    continue;
                }
                nodeJvm.setTestResource(testResource);
                availableNodes.add(nodeJvm);
            }
        }
        if (CollectionUtils.isEmpty(availableNodes)) {
            MSException.throwException("未获取到资源池，请检查配置【系统设置-系统-测试资源池】");
        }
        return availableNodes;
    }
}
