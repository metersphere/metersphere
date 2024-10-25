package io.metersphere.system.service;

import io.metersphere.engine.EngineFactory;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.dto.pool.ResourcePoolNodeMetric;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.pool.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.QueryResourcePoolRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.*;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.TaskRunnerClient;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TestResourcePoolService {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private ExtResourcePoolMapper extResourcePoolMapper;

    private final static String poolControllerUrl = "http://%s:%s/metric";


    public void checkAndSaveOrgRelation(TestResourcePool testResourcePool, String id, TestResourceDTO testResourceDTO) {
        //防止前端传入的应用组织为空
        if ((testResourcePool.getAllOrg() == null || !testResourcePool.getAllOrg()) && CollectionUtils.isEmpty(testResourceDTO.getOrgIds())) {
            throw new MSException(Translator.get("resource_pool_application_organization_is_empty"));
        }

        //前端应用组织选择了全部，但是也传了部分组织，以全部组织为主
        if ((testResourcePool.getAllOrg() != null && testResourcePool.getAllOrg()) && CollectionUtils.isNotEmpty(testResourceDTO.getOrgIds())) {
            testResourceDTO.setOrgIds(new ArrayList<>());
        }

        //前端选择部分组织保存资源池与组织关系
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestResourcePoolOrganizationMapper poolOrganizationMapper = sqlSession.getMapper(TestResourcePoolOrganizationMapper.class);
        if ((testResourcePool.getAllOrg() == null || !testResourcePool.getAllOrg()) && CollectionUtils.isNotEmpty(testResourceDTO.getOrgIds())) {
            testResourcePool.setAllOrg(false);
            testResourceDTO.getOrgIds().forEach(orgId -> {
                TestResourcePoolOrganization testResourcePoolOrganization = new TestResourcePoolOrganization();
                testResourcePoolOrganization.setId(IDGenerator.nextStr());
                testResourcePoolOrganization.setOrgId(orgId);
                testResourcePoolOrganization.setTestResourcePoolId(id);
                poolOrganizationMapper.insert(testResourcePoolOrganization);
            });
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private static boolean checkNodeOrK8s(TestResourceDTO testResourceDTO, String type) {
        if (StringUtils.equalsIgnoreCase(type, ResourcePoolTypeEnum.NODE.getName())) {
            NodeResourcePoolService resourcePoolService = CommonBeanFactory.getBean(NodeResourcePoolService.class);
            if (resourcePoolService != null) {
                return resourcePoolService.validate(testResourceDTO);
            } else {
                return false;
            }
        } else {
            return EngineFactory.validateNamespaceExists(testResourceDTO);
        }
    }


    public boolean checkApiConfig(TestResourceDTO testResourceDTO, TestResourcePool testResourcePool, String type) {
        boolean validate = checkNodeOrK8s(testResourceDTO, type);
        testResourcePool.setEnable(validate);
        return validate;
    }

    public void updateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        checkTestResourcePool(testResourcePool);
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        TestResourceDTO testResourceDTO = testResourcePool.getTestResourceDTO();
        checkAndSaveOrgRelation(testResourcePool, testResourcePool.getId(), testResourceDTO);
        checkApiConfig(testResourceDTO, testResourcePool, testResourcePool.getType());

        if (CollectionUtils.isEmpty(testResourceDTO.getNodesList())) {
            testResourceDTO.setNodesList(new ArrayList<>());
        }
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.NODE.getName())) {
            TestResourcePoolValidateService testResourcePoolValidateService = CommonBeanFactory.getBean(TestResourcePoolValidateService.class);
            if (testResourcePoolValidateService != null) {
                testResourcePoolValidateService.validateNodeList(testResourceDTO.getNodesList());
            }
        }
        String configuration = JSON.toJSONString(testResourceDTO);
        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(testResourcePool.getId());
        testResourcePoolBlob.setConfiguration(configuration.getBytes());

        testResourcePoolBlobMapper.updateByPrimaryKeySelective(testResourcePoolBlob);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public List<TestResourcePoolDTO> listResourcePools(QueryResourcePoolRequest request) {
        List<TestResourcePool> testResourcePools = extResourcePoolMapper.getResourcePoolList(request);
        List<TestResourcePoolDTO> testResourcePoolDTOS = new ArrayList<>();
        testResourcePools.forEach(pool -> {
            TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(pool.getId());
            byte[] configuration = testResourcePoolBlob.getConfiguration();
            String testResourceDTOStr = new String(configuration);
            TestResourceDTO testResourceDTO = JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
            TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
            BeanUtils.copyBean(testResourcePoolDTO, pool);
            testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
            TestResourcePoolOrganizationExample testResourcePoolOrganizationExample = new TestResourcePoolOrganizationExample();
            testResourcePoolOrganizationExample.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            List<TestResourcePoolOrganization> testResourcePoolOrganizations = testResourcePoolOrganizationMapper.selectByExample(testResourcePoolOrganizationExample);
            if (pool.getAllOrg() || CollectionUtils.isNotEmpty(testResourcePoolOrganizations)) {
                testResourcePoolDTO.setInUsed(true);
            }
            //增加组织名称
            if (pool.getAllOrg()) {
                testResourcePoolDTO.setOrgNames(List.of(Translator.get("all_organization")));
            } else if (CollectionUtils.isNotEmpty(testResourcePoolOrganizations)) {
                List<String> orgIds = testResourcePoolOrganizations.stream().map(TestResourcePoolOrganization::getOrgId).distinct().toList();
                OrganizationExample organizationExample = new OrganizationExample();
                organizationExample.createCriteria().andIdIn(orgIds);
                List<Organization> organizations = organizationMapper.selectByExample(organizationExample);
                List<String> orgNameList = organizations.stream().map(Organization::getName).distinct().toList();
                testResourcePoolDTO.setOrgNames(orgNameList);
            }
            //获取最大并发
            if (StringUtils.equalsIgnoreCase(pool.getType(), ResourcePoolTypeEnum.NODE.getName())) {
                int maxConcurrentNumber = 0;
                for (TestResourceNodeDTO testResourceNodeDTO : testResourceDTO.getNodesList()) {
                    maxConcurrentNumber = maxConcurrentNumber + testResourceNodeDTO.getConcurrentNumber();
                }
                testResourcePoolDTO.setMaxConcurrentNumber(maxConcurrentNumber);
            } else {
                //处理k8s资源池
                testResourcePoolDTO.setMaxConcurrentNumber(testResourceDTO.getConcurrentNumber());
            }
            testResourcePoolDTOS.add(testResourcePoolDTO);
        });

        return testResourcePoolDTOS;
    }

    public ResourcePoolNodeMetric getNodeMetric(String ip, String port) {
        ResourcePoolNodeMetric resourcePoolNodeMetric = new ResourcePoolNodeMetric();
        try {
            ResultHolder body = TaskRunnerClient.get(String.format(poolControllerUrl, ip, port));
            if (body == null) {
                return null;
            }
            if (body.getData() != null && StringUtils.equalsIgnoreCase("OK", body.getData().toString())) {
                return null;
            }
            resourcePoolNodeMetric = JSON.parseObject(JSON.toJSONString(body.getData()), ResourcePoolNodeMetric.class);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }

        return resourcePoolNodeMetric;
    }

    public void checkTestResourcePool(TestResourcePool testResourcePool) {
        String resourcePoolName = testResourcePool.getName();
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

    public void getTestResourceDTO(String resourcePoolId) {
        TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(resourcePoolId);
        String testResourceDTOStr = new String(testResourcePoolBlob.getConfiguration());
        JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
    }

    public TestResourcePoolReturnDTO getTestResourcePoolDetail(String testResourcePoolId) {
        TestResourcePoolReturnDTO testResourcePoolReturnDTO = new TestResourcePoolReturnDTO();
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePool == null) {
            throw new MSException(Translator.get("test_resource_pool_not_exists"));
        }
        TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePoolBlob == null) {
            BeanUtils.copyBean(testResourcePoolReturnDTO, testResourcePool);
            return testResourcePoolReturnDTO;
        }
        byte[] configuration = testResourcePoolBlob.getConfiguration();
        String testResourceDTOStr = new String(configuration);
        TestResourceDTO testResourceDTO = JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
        if (CollectionUtils.isEmpty(testResourceDTO.getNodesList())) {
            testResourceDTO.setNodesList(new ArrayList<>());
        } else {
            for (TestResourceNodeDTO testResourceNodeDTO : testResourceDTO.getNodesList()) {
                if (testResourceNodeDTO.getSingleTaskConcurrentNumber() == null) {
                    testResourceNodeDTO.setSingleTaskConcurrentNumber(3);
                }
            }
        }
        TestResourceReturnDTO testResourceReturnDTO = new TestResourceReturnDTO();
        BeanUtils.copyBean(testResourceReturnDTO, testResourceDTO);
        List<String> orgIds = testResourceDTO.getOrgIds();
        List<OptionDTO> orgIdNameMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orgIds)) {
            for (String orgId : orgIds) {
                OptionDTO optionDTO = new OptionDTO();
                Organization organization = organizationMapper.selectByPrimaryKey(orgId);
                optionDTO.setId(orgId);
                if (organization != null) {
                    optionDTO.setName(organization.getName());
                } else {
                    optionDTO.setName(Translator.get("organization_not_exists"));
                }
                orgIdNameMap.add(optionDTO);
            }
        }
        testResourceReturnDTO.setOrgIdNameMap(orgIdNameMap);
        BeanUtils.copyBean(testResourcePoolReturnDTO, testResourcePool);
        testResourcePoolReturnDTO.setTestResourceReturnDTO(testResourceReturnDTO);
        return testResourcePoolReturnDTO;
    }

    public TestResourcePool getTestResourcePool(String testResourcePoolId) {
        return testResourcePoolMapper.selectByPrimaryKey(testResourcePoolId);
    }

    public LogDTO updateLog(TestResourcePoolRequest request) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(request.getId());
        if (pool != null) {
            LogDTO dto = new LogDTO(
                    OperationLogConstants.SYSTEM,
                    OperationLogConstants.SYSTEM,
                    pool.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_RESOURCE_POOL,
                    request.getName());

            dto.setPath("/update");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(pool));
            return dto;
        }
        return null;
    }

    /**
     * 校验该组织是否有权限使用该资源池
     *
     * @param resourcePool 资源池对象
     * @param orgId        组织id
     * @return boolean
     */
    public boolean validateOrgResourcePool(TestResourcePool resourcePool, String orgId) {
        if (BooleanUtils.isTrue(resourcePool.getAllOrg())) {
            return true;
        }
        TestResourcePoolOrganizationExample example = new TestResourcePoolOrganizationExample();
        example.createCriteria()
                .andTestResourcePoolIdEqualTo(resourcePool.getId())
                .andOrgIdEqualTo(orgId);
        return testResourcePoolOrganizationMapper.countByExample(example) >= 1;
    }

    public ResourcePoolNodeMetric getTestResourcePoolCapacityDetail(TestResourcePoolCapacityRequest request) {
        ResourcePoolNodeMetric resourcePoolNodeMetric = new ResourcePoolNodeMetric();
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(request.getPoolId());
        if (testResourcePool == null || !testResourcePool.getEnable() || testResourcePool.getDeleted()) {
            return new ResourcePoolNodeMetric();
        }
        TestResourcePoolBlob testResourcePoolBlob = testResourcePoolBlobMapper.selectByPrimaryKey(request.getPoolId());
        byte[] configuration = testResourcePoolBlob.getConfiguration();
        String testResourceDTOStr = new String(configuration);
        TestResourceDTO testResourceDTO = JSON.parseObject(testResourceDTOStr, TestResourceDTO.class);
        if (CollectionUtils.isEmpty(testResourceDTO.getNodesList())) {
            return new ResourcePoolNodeMetric();
        }
        if (StringUtils.isBlank(request.getIp())) {
            int concurrentNumber = 0;
            int occupiedConcurrentNumber = 0;
            for (TestResourceNodeDTO testResourceNodeDTO : testResourceDTO.getNodesList()) {
                ResourcePoolNodeMetric nodeMetric = getNodeMetric(testResourceNodeDTO.getIp(), testResourceNodeDTO.getPort());
                concurrentNumber = concurrentNumber + testResourceNodeDTO.getConcurrentNumber();
                occupiedConcurrentNumber = occupiedConcurrentNumber + nodeMetric.getOccupiedConcurrentNumber();
            }
            resourcePoolNodeMetric.setConcurrentNumber(concurrentNumber);
            resourcePoolNodeMetric.setOccupiedConcurrentNumber(occupiedConcurrentNumber);
        } else {
            resourcePoolNodeMetric = getNodeMetric(request.getIp(), request.getPort());
            for (TestResourceNodeDTO testResourceNodeDTO : testResourceDTO.getNodesList()) {
                if (StringUtils.equals(testResourceNodeDTO.getIp(), request.getIp()) && StringUtils.equals(testResourceNodeDTO.getPort(), request.getPort())) {
                    resourcePoolNodeMetric.setConcurrentNumber(testResourceNodeDTO.getConcurrentNumber());
                    break;
                }
            }
        }
        return resourcePoolNodeMetric;
    }
}
