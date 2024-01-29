package io.metersphere.project.controller;

import io.metersphere.project.service.CleanupEnvironmentResourceService;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupRelation;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentGroupRelationMapper;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupEnvironmentTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CleanupEnvironmentResourceService cleanupEnvironmentResourceService;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private EnvironmentGroupRelationMapper environmentGroupRelationMapper;

    @Autowired
    public CleanupEnvironmentTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        EnvironmentGroup environmentGroup = new EnvironmentGroup();
        environmentGroup.setId("test");
        environmentGroup.setPos(1L);
        environmentGroup.setName("test");
        environmentGroup.setProjectId("test");
        environmentGroup.setUpdateUser("admin");
        environmentGroup.setCreateUser("admin");
        environmentGroup.setCreateTime(System.currentTimeMillis());
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroupMapper.insert(environmentGroup);
        EnvironmentGroupRelation environmentGroupRelation = new EnvironmentGroupRelation();
        environmentGroupRelation.setId("test");
        environmentGroupRelation.setEnvironmentGroupId("test");
        environmentGroupRelation.setEnvironmentId("test");
        environmentGroupRelation.setProjectId("test");
        environmentGroupRelationMapper.insert(environmentGroupRelation);
        serviceInvoker.invokeServices("test");
    }

}
