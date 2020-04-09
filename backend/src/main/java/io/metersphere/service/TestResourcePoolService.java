package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.base.mapper.ext.ExtTestReourcePoolMapper;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.ResourceStatusEnum.VALID;

/**
 * @author dongbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourcePoolService {

    private final static String nodeControllerUrl = "http://%s:%s/status";

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourceMapper testResourceMapper;
    @Resource
    private ExtTestReourcePoolMapper extTestReourcePoolMapper;
    @Resource
    private RestTemplate restTemplate;

    public TestResourcePoolDTO addTestResourcePool(TestResourcePoolDTO testResourcePool) {
        testResourcePool.setId(UUID.randomUUID().toString());
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setStatus(VALID.name());
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.insertSelective(testResourcePool);
        return testResourcePool;
    }

    public void deleteTestResourcePool(String testResourcePoolId) {
        deleteTestResource(testResourcePoolId);
        testResourcePoolMapper.deleteByPrimaryKey(testResourcePoolId);
    }

    public void updateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public List<TestResourcePoolDTO> listResourcePools(QueryResourcePoolRequest request) {
        return extTestReourcePoolMapper.listResourcePools(request);
    }

    private void validateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.K8S.name())) {
            validateK8s(testResourcePool);
            return;
        }
        validateNodes(testResourcePool);
    }

    private void validateNodes(TestResourcePoolDTO testResourcePool) {
        if (CollectionUtils.isEmpty(testResourcePool.getResources())) {
            MSException.throwException(Translator.get("no_nodes_message"));
        }

        deleteTestResource(testResourcePool.getId());
        List<String> nodeIps = testResourcePool.getResources().stream()
                .map(resource -> {
                    NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                    return nodeDTO.getIp();
                })
                .distinct()
                .collect(Collectors.toList());
        if (nodeIps.size() < testResourcePool.getResources().size()) {
            MSException.throwException(Translator.get("duplicate_node_ip"));
        }
        for (TestResource resource : testResourcePool.getResources()) {
            NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
            boolean isValidate = validateNode(nodeDTO);
            if (!isValidate) {
                testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
                resource.setStatus(ResourceStatusEnum.INVALID.name());
            } else {
                resource.setStatus(VALID.name());
            }
            resource.setTestResourcePoolId(testResourcePool.getId());
            updateTestResource(resource);

        }
    }

    private boolean validateNode(NodeDTO node) {
        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(String.format(nodeControllerUrl, node.getIp(), node.getPort()), String.class);
            return HttpStatus.OK.equals(entity.getStatusCode());
        } catch (Exception e) {
            return false;
        }
    }

    private void validateK8s(TestResourcePoolDTO testResourcePool) {

        if (CollectionUtils.isEmpty(testResourcePool.getResources()) || testResourcePool.getResources().size() != 1) {
            throw new RuntimeException(Translator.get("only_one_k8s"));
        }

        TestResource testResource = testResourcePool.getResources().get(0);
        testResource.setTestResourcePoolId(testResourcePool.getId());
        try {
            KubernetesProvider provider = new KubernetesProvider(testResource.getConfiguration());
            provider.validateCredential();
            testResource.setStatus(VALID.name());
        } catch (Exception e) {
            testResource.setStatus(ResourceStatusEnum.INVALID.name());
            testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
        }
        deleteTestResource(testResourcePool.getId());
        updateTestResource(testResource);

    }

    private void updateTestResource(TestResource testResource) {
        testResource.setUpdateTime(System.currentTimeMillis());
        testResource.setCreateTime(System.currentTimeMillis());
        testResource.setId(UUID.randomUUID().toString());
        testResourceMapper.insertSelective(testResource);
    }

    private void deleteTestResource(String testResourcePoolId) {
        TestResourceExample testResourceExample = new TestResourceExample();
        testResourceExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolId);
        testResourceMapper.deleteByExample(testResourceExample);
    }

    public TestResourcePool getResourcePool(String resourcePoolId) {
        return testResourcePoolMapper.selectByPrimaryKey(resourcePoolId);
    }

    public List<TestResourcePool> listValidResourcePools() {
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo(ResourceStatusEnum.VALID.name());
        return testResourcePoolMapper.selectByExample(example);
    }
}
