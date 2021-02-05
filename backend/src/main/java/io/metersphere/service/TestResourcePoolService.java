package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.ResourceStatusEnum.*;

/**
 * @author dongbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourcePoolService {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourceMapper testResourceMapper;
    @Resource
    private NodeResourcePoolService nodeResourcePoolService;

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
        updateTestResourcePoolStatus(testResourcePoolId, DELETE.name());
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
        // 禁用/删除 资源池
        if (INVALID.name().equals(status) || DELETE.name().equals(status)) {
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
            LogUtil.error(e.getMessage(), e);
        }
    }

    public List<TestResourcePoolDTO> listResourcePools(QueryResourcePoolRequest request) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            criteria.andStatusEqualTo(request.getStatus());
        }
        criteria.andStatusNotEqualTo(DELETE.name());
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
                LogUtil.error(e.getMessage(), e);
            }
        });
        return testResourcePoolDTOS;
    }

    private boolean validateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.K8S.name())) {
            KubernetesResourcePoolService resourcePoolService = CommonBeanFactory.getBean(KubernetesResourcePoolService.class);
            if (resourcePoolService == null) {
                return false;
            }
            return resourcePoolService.validate(testResourcePool);
        }
        return nodeResourcePoolService.validate(testResourcePool);
    }

    private void deleteTestResource(String testResourcePoolId) {
        TestResourceExample testResourceExample = new TestResourceExample();
        testResourceExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolId);
        testResourceMapper.deleteByExample(testResourceExample);
    }

    public TestResourcePool getResourcePool(String resourcePoolId) {
        return testResourcePoolMapper.selectByPrimaryKey(resourcePoolId);
    }

    public List<TestResourcePoolDTO> listValidResourcePools() {
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
        request.setStatus(VALID.name());
        return listResourcePools(request);
    }

    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return filterQuota(listValidResourcePools());
    }

    private List<TestResourcePoolDTO> filterQuota(List<TestResourcePoolDTO> list) {
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
