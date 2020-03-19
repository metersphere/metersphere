package io.metersphere.service;

import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author dongbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourcePoolService {

    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    public TestResourcePool addTestResourcePool(TestResourcePool testResourcePool) {
        testResourcePool.setId(UUID.randomUUID().toString());
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setStatus("1");
        testResourcePoolMapper.insertSelective(testResourcePool);
        return testResourcePool;
    }

    public void deleteTestResourcePool(String testResourcePoolId) {
        testResourcePoolMapper.deleteByPrimaryKey(testResourcePoolId);
    }

    public void updateTestResourcePool(TestResourcePool testResourcePool) {
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
    }

    public List<TestResourcePool> listResourcePools(QueryResourcePoolRequest request) {
        TestResourcePoolExample example = new TestResourcePoolExample();
        if (!StringUtils.isEmpty(request.getName())) {
            example.createCriteria().andNameLike("%" + request.getName() + "%");
        }
        return testResourcePoolMapper.selectByExample(example);
    }
}
