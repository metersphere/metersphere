package io.metersphere.service;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.mapper.TestResourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResourceService {

    @Resource
    private TestResourceMapper testResourceMapper;

    public List<TestResource> getTestResourceList(String testResourcePoolId) {
        TestResourceExample testResourceExample = new TestResourceExample();
        testResourceExample.createCriteria().andTestResourcePoolIdEqualTo(testResourcePoolId);
        return testResourceMapper.selectByExampleWithBLOBs(testResourceExample);
    }

    public List<TestResource> getResourcesByPoolId(String resourcePoolId) {
        TestResourceExample example = new TestResourceExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId);
        return testResourceMapper.selectByExampleWithBLOBs(example);
    }

    public TestResource getTestResource(String resourceId) {
        return testResourceMapper.selectByPrimaryKey(resourceId);
    }
}
