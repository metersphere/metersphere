package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.dto.UpdatePoolDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
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
        criteria.andStatusNotEqualTo(DELETE.name());

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

    /**
     * 禁用资源池时，检查是否有测试正在使用
     *
     * @param poolId 资源池ID
     * @return UpdatePoolDTO
     */
    public UpdatePoolDTO checkHaveTestUsePool(String poolId) {
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(poolId);
        if (testResourcePool == null) {
            MSException.throwException("Resource Pool not found.");
        }
        UpdatePoolDTO result = new UpdatePoolDTO();
        StringBuilder builder = new StringBuilder();
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andTestResourcePoolIdEqualTo(poolId);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        if (CollectionUtils.isNotEmpty(loadTests)) {
            loadTests.forEach(loadTest -> {
                String testStatus = loadTest.getStatus();
                if (StringUtils.equalsAny(testStatus, PerformanceTestStatus.Starting.name(),
                        PerformanceTestStatus.Running.name(), PerformanceTestStatus.Reporting.name())) {
                    builder.append(loadTest.getName()).append("; ");
                    result.setHaveTestUsePool(true);
                }
            });
        }
        result.setTestName(builder.toString());
        return result;
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

    public String getLogDetails(String id) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(id);
        if (pool != null) {
            TestResourceExample example = new TestResourceExample();
            example.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            List<TestResource> resources = testResourceMapper.selectByExampleWithBLOBs(example);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(pool, SystemReference.poolColumns);
            if (pool.getType().equals("NODE")) {
                for (TestResource resource : resources) {
                    JSONObject object = JSONObject.parseObject(resource.getConfiguration());
                    DetailColumn ip = new DetailColumn("IP", "ip", object.get("ip"), null);
                    columns.add(ip);
                    DetailColumn port = new DetailColumn("Port", "port", object.get("port"), null);
                    columns.add(port);
                    DetailColumn monitorPort = new DetailColumn("Monitor", "monitorPort", object.get("monitorPort"), null);
                    columns.add(monitorPort);
                    DetailColumn maxConcurrency = new DetailColumn("最大并发数", "maxConcurrency", object.get("maxConcurrency"), null);
                    columns.add(maxConcurrency);
                }
            } else {
                if (CollectionUtils.isNotEmpty(resources)) {
                    TestResource resource = resources.get(0);
                    JSONObject object = JSONObject.parseObject(resource.getConfiguration());
                    DetailColumn masterUrl = new DetailColumn("Master Url", "masterUrl", object.get("masterUrl"), null);
                    columns.add(masterUrl);
                    DetailColumn token = new DetailColumn("Token", "token", object.get("token"), null);
                    columns.add(token);
                    DetailColumn monitorPort = new DetailColumn("Namespace", "namespace", object.get("namespace"), null);
                    columns.add(monitorPort);
                    DetailColumn maxConcurrency = new DetailColumn("最大并发数", "maxConcurrency", object.get("maxConcurrency"), null);
                    columns.add(maxConcurrency);
                    DetailColumn podThreadLimit = new DetailColumn("单POD最大线程数", "podThreadLimit", object.get("podThreadLimit"), null);
                    columns.add(podThreadLimit);
                    DetailColumn nodeSelector = new DetailColumn("nodeSelector", "nodeSelector", object.get("nodeSelector"), null);
                    columns.add(nodeSelector);
                }
            }

            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(pool.getId()), null, pool.getName(), pool.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
