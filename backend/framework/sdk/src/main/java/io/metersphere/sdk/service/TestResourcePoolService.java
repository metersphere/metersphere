package io.metersphere.sdk.service;

import groovy.util.logging.Slf4j;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.dto.QueryResourcePoolRequest;
import io.metersphere.sdk.dto.TestResourceDTO;
import io.metersphere.sdk.dto.TestResourcePoolDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
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
    @Resource
    private SqlSessionFactory sqlSessionFactory;


    public TestResourcePool addTestResourcePool(TestResourcePoolDTO testResourcePool) {
        String id = UUID.randomUUID().toString();

        checkTestResourcePool(testResourcePool);

        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(id);
        TestResourceDTO testResourceDTO = testResourcePool.getTestResourceDTO();
        checkAndSaveOrgRelation(testResourcePool, id, testResourceDTO);

        checkApiConfig(testResourceDTO, testResourcePool, testResourcePool.getType());
        checkLoadConfig(testResourceDTO, testResourcePool, testResourcePool.getType());
        checkUiConfig(testResourceDTO, testResourcePool);
        String configuration = JSON.toJSONString(testResourceDTO);
        testResourcePoolBlob.setConfiguration(configuration.getBytes());

        buildTestPoolBaseInfo(testResourcePool, id);
        testResourcePoolMapper.insert(testResourcePool);
        testResourcePoolBlobMapper.insert(testResourcePoolBlob);
        testResourcePool.setId(id);
        return testResourcePool;
    }

    private void checkAndSaveOrgRelation(TestResourcePool testResourcePool, String id, TestResourceDTO testResourceDTO) {
        //防止前端传入的应用组织为空
        if ((testResourcePool.getAllOrg() == null || !testResourcePool.getAllOrg())&& CollectionUtils.isEmpty(testResourceDTO.getOrgIds())){
            throw new MSException(Translator.get("resource_pool_application_organization_is_empty"));
        }

        //前端应用组织选择了全部，但是也传了部分组织，以全部组织为主
        if ((testResourcePool.getAllOrg() != null && testResourcePool.getAllOrg()) && CollectionUtils.isNotEmpty(testResourceDTO.getOrgIds()) ) {
            testResourceDTO.setOrgIds(new ArrayList<>());
        }

        //前端选择部分组织保存资源池与组织关系
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestResourcePoolOrganizationMapper poolOrganizationMapper = sqlSession.getMapper(TestResourcePoolOrganizationMapper.class);
        if ((testResourcePool.getAllOrg() == null || !testResourcePool.getAllOrg())&& CollectionUtils.isNotEmpty(testResourceDTO.getOrgIds())){
            testResourcePool.setAllOrg(false);
            testResourceDTO.getOrgIds().forEach(orgId->{
                TestResourcePoolOrganization testResourcePoolOrganization = new TestResourcePoolOrganization();
                testResourcePoolOrganization.setId(UUID.randomUUID().toString());
                testResourcePoolOrganization.setOrgId(orgId);
                testResourcePoolOrganization.setTestResourcePoolId(id);
                poolOrganizationMapper.insert(testResourcePoolOrganization);
            });
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private static void buildTestPoolBaseInfo(TestResourcePool testResourcePool, String id) {
        testResourcePool.setId(id);
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setEnable(true);
        testResourcePool.setDeleted(false);
    }

    private boolean checkLoadConfig(TestResourceDTO testResourceDTO, TestResourcePool testResourcePool, String type) {
        if (testResourcePool.getLoadTest() == null || !testResourcePool.getLoadTest()) {
            return true;
        }
        LoadResourceService resourcePoolService = CommonBeanFactory.getBean(LoadResourceService.class);
        if (resourcePoolService == null) {
            return false;
        }
        return resourcePoolService.validate(testResourceDTO,type);
    }

    private boolean checkUiConfig(TestResourceDTO testResourceDTO, TestResourcePool testResourcePool) {
        if (testResourcePool.getUiTest() == null || !testResourcePool.getUiTest()) {
            return true;
        }
        UiResourceService resourcePoolService = CommonBeanFactory.getBean(UiResourceService.class);
        if (resourcePoolService == null) {
            return false;
        }
        return resourcePoolService.validate(testResourceDTO);
    }

    private boolean checkApiConfig(TestResourceDTO testResourceDTO, TestResourcePool testResourcePool, String type) {
        if (testResourcePool.getApiTest() == null || !testResourcePool.getApiTest()) {
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
            throw new MSException(Translator.get("test_resource_pool_not_exists"));
        }
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setEnable(false);
        testResourcePool.setDeleted(true);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public void updateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        checkTestResourcePool(testResourcePool);
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        TestResourceDTO testResourceDTO = testResourcePool.getTestResourceDTO();
        checkAndSaveOrgRelation(testResourcePool, testResourcePool.getId(), testResourceDTO);
        checkApiConfig(testResourceDTO, testResourcePool, testResourcePool.getType());
        checkLoadConfig(testResourceDTO, testResourcePool, testResourcePool.getType());
        checkUiConfig(testResourceDTO, testResourcePool);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
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
            byte[] configuration = testResourcePoolBlob.getConfiguration();
            String testResourceDTOStr = new String(configuration);
            TestResourceDTO testResourceDTO = JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
            TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
            BeanUtils.copyBean(testResourcePoolDTO, pool);
            testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
            testResourcePoolDTOS.add(testResourcePoolDTO);
        });
        return testResourcePoolDTOS;
    }

    public void checkTestResourcePool(TestResourcePool testResourcePool) {
        String resourcePoolName = testResourcePool.getName();
        if (StringUtils.isBlank(resourcePoolName)) {
            throw new MSException(Translator.get("test_resource_pool_name_is_null"));
        }
        if (StringUtils.isBlank(testResourcePool.getType())) {
            throw new MSException(Translator.get("test_resource_pool_type_is_null"));
        }
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(resourcePoolName);
        if (StringUtils.isNotBlank(testResourcePool.getId())) {
            criteria.andIdNotEqualTo(testResourcePool.getId());
        }
        criteria.andDeletedEqualTo(false);
        if (testResourcePoolMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("test_resource_pool_name_already_exists"));
        }
    }

    public TestResourcePoolDTO getTestResourcePoolDetail(String testResourcePoolId) {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePool == null) {
            throw new MSException(Translator.get("test_resource_pool_not_exists"));
        }
        TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePoolBlob == null) {
            BeanUtils.copyBean(testResourcePoolDTO, testResourcePool);
            return testResourcePoolDTO;
        }
        byte[] configuration = testResourcePoolBlob.getConfiguration();
        String testResourceDTOStr = new String(configuration);
        TestResourceDTO testResourceDTO = JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
        BeanUtils.copyBean(testResourcePoolDTO, testResourcePool);
        testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
        return testResourcePoolDTO;
    }

    public String getLogDetails(String id) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(id);
        if (pool != null) {
            return pool.getName();
        }
        return null;
    }
}
