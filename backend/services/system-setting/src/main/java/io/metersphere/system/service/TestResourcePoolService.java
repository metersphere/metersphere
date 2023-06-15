package io.metersphere.system.service;

import groovy.util.logging.Slf4j;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.dto.ResourcePoolTypeEnum;
import io.metersphere.system.dto.TestResourceDTO;
import io.metersphere.system.dto.TestResourcePoolDTO;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
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
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;

    public TestResourcePoolDTO addTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        String id = UUID.randomUUID().toString();

        checkTestResourcePool(testResourcePoolDTO);

        buildTestPoolBaseInfo(testResourcePoolDTO, id);

        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(id);

        String configuration = testResourcePoolDTO.getConfiguration();
        TestResourceDTO testResourceDTO = JSON.parseObject(configuration, TestResourceDTO.class);

        CheckApiConfig(testResourceDTO, testResourcePoolDTO.getApiTest(), testResourcePoolDTO.getType());
        CheckLoadConfig(testResourceDTO, testResourcePoolDTO.getLoadTest(), testResourcePoolDTO.getType());
        CheckUiConfig(testResourceDTO, testResourcePoolDTO.getUiTest());

        testResourcePoolBlob.setConfiguration(configuration.getBytes());

        testResourcePoolMapper.insertSelective(testResourcePoolDTO);
        testResourcePoolBlobMapper.insertSelective(testResourcePoolBlob);
        return testResourcePoolDTO;
    }

    private static void buildTestPoolBaseInfo(TestResourcePoolDTO testResourcePoolDTO, String id) {
        testResourcePoolDTO.setId(id);
        testResourcePoolDTO.setCreateTime(System.currentTimeMillis());
        testResourcePoolDTO.setUpdateTime(System.currentTimeMillis());
        testResourcePoolDTO.setEnable(true);
        testResourcePoolDTO.setDeleted(false);
    }

    private boolean CheckLoadConfig(TestResourceDTO testResourceDTO, Boolean loadTest, String type) {
        if (!loadTest) {
            return true;
        }
        LoadResourceService resourcePoolService = CommonBeanFactory.getBean(LoadResourceService.class);
        if (resourcePoolService == null) {
            return false;
        }
        return resourcePoolService.validate(testResourceDTO,type);
    }

    private boolean CheckUiConfig(TestResourceDTO testResourceDTO, Boolean uiTest) {
        if (!uiTest) {
            return true;
        }
        UiResourceService resourcePoolService = CommonBeanFactory.getBean(UiResourceService.class);
        if (resourcePoolService == null) {
            return false;
        }
        return resourcePoolService.validate(testResourceDTO);
    }

    private boolean CheckApiConfig(TestResourceDTO testResourceDTO, Boolean apiTest, String type) {
        if (!apiTest) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(type,ResourcePoolTypeEnum.NODE.name())) {
            NodeResourcePoolService resourcePoolService = CommonBeanFactory.getBean(NodeResourcePoolService.class);
            return resourcePoolService.validate(testResourceDTO);
        } else {
            KubernetesResourcePoolService resourcePoolService = CommonBeanFactory.getBean(KubernetesResourcePoolService.class);
            if (resourcePoolService == null) {
                return false;
            }
            return resourcePoolService.validate(testResourceDTO);
        }
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
        testResourcePoolDTO.setCreateUser(null);
        testResourcePoolDTO.setUpdateTime(System.currentTimeMillis());
        String configuration = testResourcePoolDTO.getConfiguration();
        TestResourceDTO testResourceDTO = JSON.parseObject(configuration, TestResourceDTO.class);
        CheckApiConfig(testResourceDTO, testResourcePoolDTO.getApiTest(), testResourcePoolDTO.getType());
        CheckLoadConfig(testResourceDTO, testResourcePoolDTO.getLoadTest(), testResourcePoolDTO.getType());
        CheckUiConfig(testResourceDTO, testResourcePoolDTO.getUiTest());
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
            TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(pool.getId());
            TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
            try {
                BeanUtils.copyProperties(testResourcePoolDTO, pool);
                testResourcePoolDTO.setConfiguration(new String(testResourcePoolBlob.getConfiguration()));
                testResourcePoolDTOS.add(testResourcePoolDTO);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtils.error(e.getMessage(), e);
            }
        });
        return testResourcePoolDTOS;
    }

    public void checkTestResourcePool(TestResourcePoolDTO testResourcePoolDTO) {
        String resourcePoolName = testResourcePoolDTO.getName();
        if (StringUtils.isBlank(resourcePoolName)) {
            throw new MSException(Translator.get("test_resource_pool_name_is_null"));
        }
        if (StringUtils.isBlank(testResourcePoolDTO.getType())) {
            throw new MSException(Translator.get("test_resource_pool_type_is_null"));
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

    public String getLogDetails(String id) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(id);
        if (pool != null) {
            return pool.getName();
        }
        return null;
    }

}
