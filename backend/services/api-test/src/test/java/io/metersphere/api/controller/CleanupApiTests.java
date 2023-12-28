package io.metersphere.api.controller;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.CleanupApiResourceService;
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
public class CleanupApiTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CleanupApiResourceService cleanupApiResourceService;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;

    @Autowired
    public CleanupApiTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public void initData() throws Exception {
        //创建接口模块
        ApiDefinitionModule apiDefinitionModule = new ApiDefinitionModule();
        apiDefinitionModule.setId("test-module");
        apiDefinitionModule.setProjectId("test");
        apiDefinitionModule.setName("test");
        apiDefinitionModule.setPos(1L);
        apiDefinitionModule.setCreateUser("admin");
        apiDefinitionModule.setUpdateUser("admin");
        apiDefinitionModule.setCreateTime(System.currentTimeMillis());
        apiDefinitionModule.setUpdateTime(System.currentTimeMillis());
        apiDefinitionModuleMapper.insertSelective(apiDefinitionModule);
        //创建场景模块
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("test-scenario-module");
        apiScenarioModule.setProjectId("test");
        apiScenarioModule.setName("test");
        apiScenarioModule.setPos(1L);
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        //创建接口
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("test");
        apiDefinition.setProjectId("test");
        apiDefinition.setModuleId("test-module");
        apiDefinition.setName("test");
        apiDefinition.setPath("test");
        apiDefinition.setProtocol("http");
        apiDefinition.setMethod("test");
        apiDefinition.setCreateUser("admin");
        apiDefinition.setUpdateUser("admin");
        apiDefinition.setNum(1L);
        apiDefinition.setVersionId("test");
        apiDefinition.setRefId("test");
        apiDefinition.setPos(1L);
        apiDefinition.setLatest(true);
        apiDefinition.setStatus("api-status");
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.insertSelective(apiDefinition);
        //创建用例
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("test");
        apiTestCase.setProjectId("test");
        apiTestCase.setApiDefinitionId("test");
        apiTestCase.setCreateUser("admin");
        apiTestCase.setUpdateUser("admin");
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setPos(1L);
        apiTestCase.setNum(1L);
        apiTestCase.setStatus("test");
        apiTestCase.setVersionId("test");
        apiTestCase.setPriority("test");
        apiTestCase.setName("test");
        apiTestCaseMapper.insertSelective(apiTestCase);
        //创建mock
        ApiDefinitionMock apiDefinitionMock = new ApiDefinitionMock();
        apiDefinitionMock.setId("test");
        apiDefinitionMock.setApiDefinitionId("test");
        apiDefinitionMock.setProjectId("test");
        apiDefinitionMock.setCreateUser("admin");
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setName("test");
        apiDefinitionMock.setExpectNum("test");
        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);


    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        initData();
        serviceInvoker.invokeServices("test");
        cleanupApiResourceService.deleteResources("test");
    }

}
