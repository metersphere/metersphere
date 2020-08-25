package io.metersphere.service;

import static io.metersphere.commons.constants.ResourceStatusEnum.INVALID;
import static io.metersphere.commons.constants.ResourceStatusEnum.VALID;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

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
    private RestTemplate restTemplateWithTimeOut;
    @Resource
    private LoadTestMapper loadTestMapper;

    public TestResourcePoolDTO addTestResourcePool(TestResourcePoolDTO testResourcePool) {
        checkTestResourcePool(testResourcePool);
        testResourcePool.setId(UUID.randomUUID().toString());
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setStatus(VALID.name());
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.insertSelective(testResourcePool);
        return testResourcePool;
    }

    public void deleteTestResourcePool(String testResourcePoolId) {
        // check test is Running Starting Error
        checkTestStatus(testResourcePoolId);
        deleteTestResource(testResourcePoolId);
        testResourcePoolMapper.deleteByPrimaryKey(testResourcePoolId);
    }

    public void checkTestStatus(String testResourcePoolId) {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria()
                .andTestResourcePoolIdEqualTo(testResourcePoolId);
        if (loadTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("test_resource_pool_is_use"));
        }
    }

    public void updateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        checkTestResourcePool(testResourcePool);
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        validateTestResourcePool(testResourcePool);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public void checkTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        String resourcePoolName = testResourcePoolDTO.getName();
        if (StringUtils.isBlank(resourcePoolName)) {
            MSException.throwException(Translator.get("test_resource_pool_name_is_null"));
        }

        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(resourcePoolName);
        if (StringUtils.isNotBlank(testResourcePoolDTO.getId())) {
            criteria.andIdNotEqualTo(testResourcePoolDTO.getId());
        }

        if (testResourcePoolMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("test_resource_pool_name_already_exists"));
        }

    }

    public void updateTestResourcePoolStatus(String poolId, String status) {
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(poolId);
        if (testResourcePool == null) {
            MSException.throwException("Resource Pool not found.");
        }
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setStatus(status);
        // 禁用资源池
        if (INVALID.name().equals(status)) {
            testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
            return;
        }
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        try {
            BeanUtils.copyProperties(testResourcePoolDTO, testResourcePool);
            TestResourceExample example2 = new TestResourceExample();
            example2.createCriteria().andTestResourcePoolIdEqualTo(poolId);
            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(example2);
            testResourcePoolDTO.setResources(testResources);
            if (validateTestResourcePool(testResourcePoolDTO)) {
                testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
            } else {
                MSException.throwException("Resource Pool is invalid.");
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogUtil.error(e);
        }
    }

    public List<TestResourcePoolDTO> listResourcePools(QueryResourcePoolRequest request) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        example.setOrderByClause("update_time desc");
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);
        List<TestResourcePoolDTO> testResourcePoolDTOS = new ArrayList<>();
        testResourcePools.forEach(pool -> {
            TestResourceExample example2 = new TestResourceExample();
            example2.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(example2);
            TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
            try {
                BeanUtils.copyProperties(testResourcePoolDTO, pool);
                testResourcePoolDTO.setResources(testResources);
                testResourcePoolDTOS.add(testResourcePoolDTO);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtil.error(e);
            }
        });
        return testResourcePoolDTOS;
    }

    private boolean validateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        return validateNodes(testResourcePool);
    }

    private boolean validateNodes(TestResourcePoolDTO testResourcePool) {
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
        testResourcePool.setStatus(VALID.name());
        boolean isValid = true;
        for (TestResource resource : testResourcePool.getResources()) {
            NodeDTO nodeDTO = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
            boolean isValidate = validateNode(nodeDTO);
            if (!isValidate) {
                testResourcePool.setStatus(ResourceStatusEnum.INVALID.name());
                resource.setStatus(ResourceStatusEnum.INVALID.name());
                isValid = false;
            } else {
                resource.setStatus(VALID.name());
            }
            resource.setTestResourcePoolId(testResourcePool.getId());
            updateTestResource(resource);
        }
        return isValid;
    }

    private boolean validateNode(NodeDTO node) {
        try {
            ResponseEntity<String> entity = restTemplateWithTimeOut.getForEntity(String.format(nodeControllerUrl, node.getIp(), node.getPort()), String.class);
            return HttpStatus.OK.equals(entity.getStatusCode());
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
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
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        List<TestResourcePoolDTO> testResourcePools = listResourcePools(request);
        // 重新校验 pool
        for (TestResourcePoolDTO pool : testResourcePools) {
            // 手动设置成无效的, 排除
            if (INVALID.name().equals(pool.getStatus())) {
                continue;
            }
            try {
                updateTestResourcePool(pool);
            } catch (MSException e) {
                pool.setStatus(INVALID.name());
                pool.setUpdateTime(System.currentTimeMillis());
                testResourcePoolMapper.updateByPrimaryKeySelective(pool);
            }
        }
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo(ResourceStatusEnum.VALID.name());
        return testResourcePoolMapper.selectByExample(example);
    }

    public List<TestResourcePool> listValidQuotaResourcePools() {
        return filterQuota(listValidResourcePools());
    }

    private List<TestResourcePool> filterQuota(List<TestResourcePool> list) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            Set<String> pools = quotaService.getQuotaResourcePools();
            if (!pools.isEmpty()) {
                return list.stream().filter(pool -> pools.contains(pool.getId())).collect(Collectors.toList());
            }
        }
        return list;
    }

}
