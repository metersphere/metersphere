package io.metersphere.system.service;

import groovy.util.logging.Slf4j;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestResource;
import io.metersphere.system.domain.TestResourceExample;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.dto.ResourcePoolTypeEnum;
import io.metersphere.system.dto.TestResourcePoolDTO;
import io.metersphere.system.mapper.TestResourceMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.request.QueryResourcePoolRequest;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TestResourcePoolService {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    @Resource
    private TestResourceMapper testResourceMapper;

    public TestResourcePoolDTO addTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        checkTestResourcePool(testResourcePoolDTO);
        testResourcePoolDTO.setId(UUID.randomUUID().toString());
        testResourcePoolDTO.setCreateTime(System.currentTimeMillis());
        testResourcePoolDTO.setUpdateTime(System.currentTimeMillis());
        testResourcePoolDTO.setEnable(true);
        testResourcePoolDTO.setDeleted(false);
        if (testResourcePoolDTO.getUiTest() != null && testResourcePoolDTO.getUiTest() && StringUtils.isBlank(testResourcePoolDTO.getGrid())) {
            throw new MSException("Please add ui grid");
        }
        validateTestResourcePool(testResourcePoolDTO);
        testResourcePoolMapper.insertSelective(testResourcePoolDTO);
        return testResourcePoolDTO;
    }

    public void deleteTestResourcePool(String testResourcePoolId) {
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePool == null) {
           throw new MSException("Resource Pool not found.");
        }
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setEnable(false);
        testResourcePool.setDeleted(true);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public void updateTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        checkTestResourcePool(testResourcePoolDTO);
        testResourcePoolDTO.setUpdateTime(System.currentTimeMillis());
        validateTestResourcePool(testResourcePoolDTO);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePoolDTO);
    }

    public List<TestResourcePoolDTO> listResourcePools(QueryResourcePoolRequest request) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        if (request.getEnable() != null) {
            criteria.andEnableEqualTo(request.getEnable());
        }
        criteria.andDeletedEqualTo(false);
        example.setOrderByClause("update_time desc");
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);
        List<TestResourcePoolDTO> testResourcePoolDTOS = new ArrayList<>();
        testResourcePools.forEach(pool -> {
            TestResourceExample resourceExample = new TestResourceExample();
            resourceExample.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            resourceExample.setOrderByClause("create_time");
            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(resourceExample);
            TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
            try {
                BeanUtils.copyProperties(testResourcePoolDTO, pool);
                testResourcePoolDTO.setTestResources(testResources);
                testResourcePoolDTOS.add(testResourcePoolDTO);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtils.error(e.getMessage(), e);
            }
        });
        return new ArrayList<>();
    }

    public void checkTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        String resourcePoolName = testResourcePoolDTO.getName();
        if (StringUtils.isBlank(resourcePoolName)) {
            throw new MSException(Translator.get("test_resource_pool_name_is_null"));
        }
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(resourcePoolName);
        if (StringUtils.isNotBlank(testResourcePoolDTO.getId())) {
            criteria.andIdNotEqualTo(testResourcePoolDTO.getId());
        }
        criteria.andDeletedEqualTo(false);
        if (testResourcePoolMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("test_resource_pool_name_already_exists"));
        }
    }

    private boolean validateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.K8S.name())) {
            KubernetesResourcePoolService resourcePoolService = CommonBeanFactory.getBean(KubernetesResourcePoolService.class);
            if (resourcePoolService == null) {
                return false;
            }
            return resourcePoolService.validate(testResourcePool);
        }
        NodeResourcePoolService resourcePoolService = CommonBeanFactory.getBean(NodeResourcePoolService.class);
        return resourcePoolService.validate(testResourcePool);
    }

    public String getLogDetails(String id) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(id);
        if (pool != null) {
            return pool.getName();
        }
        return null;
    }

}
