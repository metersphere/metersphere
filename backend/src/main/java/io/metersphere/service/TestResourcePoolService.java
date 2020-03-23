package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.KubernetesDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author dongbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourcePoolService {

    private final static String nodeControllerUrl = "%s:%s/status";

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    public TestResourcePool addTestResourcePool(TestResourcePool testResourcePool) {
        testResourcePool.setId(UUID.randomUUID().toString());
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setStatus("1");
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.insertSelective(testResourcePool);
        return testResourcePool;
    }

    public void deleteTestResourcePool(String testResourcePoolId) {
        testResourcePoolMapper.deleteByPrimaryKey(testResourcePoolId);
    }

    public void updateTestResourcePool(TestResourcePool testResourcePool) {
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public List<TestResourcePool> listResourcePools(QueryResourcePoolRequest request) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        if (!StringUtils.isEmpty(request.getName())) {
            example.createCriteria().andNameLike("%" + request.getName() + "%");
        }
        return testResourcePoolMapper.selectByExample(example);
    }

    private void validateTestResourcePool(TestResourcePool testResourcePool) {
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.K8S.name())) {
            validateK8s(testResourcePool);
            return;
        }
        validateNodes(testResourcePool);
    }

    private void validateNodes(TestResourcePool testResourcePool) {
        List<NodeDTO> nodes = JSON.parseArray(testResourcePool.getInfo(), NodeDTO.class);

        if (CollectionUtils.isEmpty(nodes)) {
            throw new RuntimeException("没有节点信息");
        }

        for (NodeDTO node : nodes) {
            boolean isValidate = validateNode(node);
            if (!isValidate) {
                testResourcePool.setStatus("0");
            }
            node.setValidate(isValidate);
        }
        testResourcePool.setInfo(JSON.toJSONString(nodes));
    }

    private boolean validateNode(NodeDTO dto) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> entity = restTemplate.getForEntity(String.format(nodeControllerUrl, dto.getIp(), dto.getPort()), String.class);
        return entity.getStatusCode().value() == HttpStatus.SC_OK;
    }

    private void validateK8s(TestResourcePool testResourcePool) {
        List<KubernetesDTO> dtos = JSON.parseArray(testResourcePool.getInfo(), KubernetesDTO.class);

        if (CollectionUtils.isEmpty(dtos) || dtos.size() != 1) {
            throw new RuntimeException("只能添加一个 K8s");
        }

        ClientCredential clientCredential = new ClientCredential();
        BeanUtils.copyBean(clientCredential, dtos.get(0));
        try {
            KubernetesProvider provider = new KubernetesProvider(JSON.toJSONString(clientCredential));
            provider.validateCredential();
            dtos.get(0).setValidate(true);
        } catch (Exception e) {
            dtos.get(0).setValidate(false);
            testResourcePool.setStatus("0");
        }
        testResourcePool.setInfo(JSON.toJSONString(dtos));
    }

    public TestResourcePool getResourcePool(String resourcePoolId) {
        return testResourcePoolMapper.selectByPrimaryKey(resourcePoolId);
    }
}
