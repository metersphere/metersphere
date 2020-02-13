package io.metersphere.service;

import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

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

    public List<TestResourcePool> getTestResourcePoolList() {
        return testResourcePoolMapper.selectByExample(null);
    }
}
