package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.CreateEnvironmentResourceService;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CreateEnvironmentTests extends BaseTest {
    @Resource
    private CreateEnvironmentResourceService createEnvironmentResourceService;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Test
    @Order(1)
    public void testCreateResource() throws Exception {
        Project initProject = new Project();
        initProject.setId(IDGenerator.nextStr());
        initProject.setNum(null);
        initProject.setOrganizationId("100001");
        initProject.setName("测试生成mock环境");
        initProject.setDescription("测试生成mock环境");
        initProject.setCreateUser("admin");
        initProject.setUpdateUser("admin");
        initProject.setCreateTime(System.currentTimeMillis());
        initProject.setUpdateTime(System.currentTimeMillis());
        initProject.setEnable(true);
        initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
        projectMapper.insertSelective(initProject);
        createEnvironmentResourceService.createResources(initProject.getId());
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(initProject.getId()).andNameEqualTo("Mock环境");
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        assert environments.size() == 1;
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(environments.getFirst().getId());
        assert environmentBlob != null;
    }

}
