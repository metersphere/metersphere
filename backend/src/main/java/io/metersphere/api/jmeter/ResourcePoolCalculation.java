package io.metersphere.api.jmeter;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourceExample;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.domain.TestResourcePoolExample;
import io.metersphere.base.mapper.TestResourceMapper;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourcePoolCalculation {
    @Resource
    TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    TestResourceMapper testResourceMapper;

    public TestResource getPool() {
        // 获取可以执行的资源池
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andStatusEqualTo("VALID").andTypeEqualTo("NODE").andNameEqualTo("赵勇资源池");
        List<TestResourcePool> pools = testResourcePoolMapper.selectByExample(example);
        // 暂时随机获取一个正常状态NODE
        TestResource testResource = null;
        if (CollectionUtils.isNotEmpty(pools)) {
            List<String> poolIds = pools.stream().map(pool -> pool.getId()).collect(Collectors.toList());
            TestResourceExample resourceExample = new TestResourceExample();
            resourceExample.createCriteria().andTestResourcePoolIdIn(poolIds);
            List<TestResource> testResources = testResourceMapper.selectByExampleWithBLOBs(resourceExample);
            int index = (int) (Math.random() * testResources.size());
            testResource = testResources.get(index);
        }
        if (testResource == null) {
            MSException.throwException(Translator.get("run_load_test_file_init_error"));
        }
        return testResource;
    }
}
