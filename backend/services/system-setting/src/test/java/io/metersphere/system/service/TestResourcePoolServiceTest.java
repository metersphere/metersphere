package io.metersphere.system.service;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-08  16:34
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestResourcePoolServiceTest extends BaseTest {

    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private CommonProjectService commonProjectService;

    @Test
    public void getTestResourceDTO() {
        testResourcePoolService.getTestResourceDTO(testResourcePoolMapper.selectByExample(new TestResourcePoolExample()).getFirst().getId());
    }

    @Test
    public void validateOrgResourcePool() {
        TestResourcePool resourcePool = testResourcePoolMapper.selectByExample(new TestResourcePoolExample()).getFirst();
        testResourcePoolService.getTestResourcePool(resourcePool.getId());
        commonProjectService.validateProjectResourcePool(resourcePool, DEFAULT_PROJECT_ID);
        testResourcePoolService.validateOrgResourcePool(resourcePool, DEFAULT_ORGANIZATION_ID);
    }
}
