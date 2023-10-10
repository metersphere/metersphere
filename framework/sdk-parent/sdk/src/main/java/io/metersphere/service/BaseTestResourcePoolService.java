package io.metersphere.service;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.metersphere.commons.constants.ResourceStatusEnum.DELETE;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTestResourcePoolService {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourceMapper testResourceMapper;


    public List<TestResourcePoolDTO> listResourcePoolById(List<String> testResourcePoolId) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        TestResourcePoolExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(testResourcePoolId).andTypeEqualTo("NODE");
        criteria.andStatusNotEqualTo(DELETE.name());

        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);
        return this.generateTestResourcePoolDTO(testResourcePools);
    }

    private List<TestResourcePoolDTO> generateTestResourcePoolDTO(List<TestResourcePool> testResourcePools) {
        List<TestResourcePoolDTO> testResourcePoolDTOS = new ArrayList<>();
        testResourcePools.forEach(pool -> {
            TestResourceExample example2 = new TestResourceExample();
            example2.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            example2.setOrderByClause("create_time");
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
        return this.generateTestResourcePoolDTO(testResourcePools);
    }


    public TestResourcePool getResourcePool(String resourcePoolId) {
        return testResourcePoolMapper.selectByPrimaryKey(resourcePoolId);
    }


    public String getLogDetails(String id) {
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(id);
        if (pool != null) {
            TestResourceExample example = new TestResourceExample();
            example.createCriteria().andTestResourcePoolIdEqualTo(pool.getId());
            example.setOrderByClause("create_time");
            List<TestResource> resources = testResourceMapper.selectByExampleWithBLOBs(example);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(pool, SystemReference.poolColumns);
            if (pool.getType().equals("NODE")) {
                for (TestResource resource : resources) {
                    Map<String, Object> object = JSON.parseObject(resource.getConfiguration(), Map.class);
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
                    Map<String, Object> object = JSON.parseObject(resource.getConfiguration(), Map.class);
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
