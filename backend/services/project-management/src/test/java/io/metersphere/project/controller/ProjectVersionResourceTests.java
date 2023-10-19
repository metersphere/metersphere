package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.domain.ProjectVersionExample;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectVersionResourceTests extends BaseTest {

    private final ProjectServiceInvoker serviceInvoker;

    public static final String TEST_PROJECT_ID = "project-version-test-id";

    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Autowired
    public ProjectVersionResourceTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    public void test() {
        ProjectVersionExample example = new ProjectVersionExample();
        example.createCriteria().andProjectIdEqualTo(TEST_PROJECT_ID);
        // 默认为空
        List<ProjectVersion> projectVersions = projectVersionMapper.selectByExample(example);
        Assertions.assertTrue(projectVersions.isEmpty());
        // 初始化项目版本资源{默认为一条版本记录V1.0, 并生成一条项目版本配置项}
        serviceInvoker.invokeCreateServices(TEST_PROJECT_ID);
        List<ProjectVersion> createVersions = projectVersionMapper.selectByExample(example);
        Assertions.assertEquals(1, createVersions.size());
        ProjectApplicationRequest request = new ProjectApplicationRequest();
        request.setProjectId(TEST_PROJECT_ID);
        Map<String, Object> configMap = projectApplicationService.get(request, List.of(ProjectApplicationType.VERSION.VERSION_ENABLE.name()));
        Assertions.assertTrue(configMap.containsKey(ProjectApplicationType.VERSION.VERSION_ENABLE.name()));
        // 清空项目版本资源{版本记录为空, 配置项为空}
        serviceInvoker.invokeServices(TEST_PROJECT_ID);
        List<ProjectVersion> cleanUpVersions = projectVersionMapper.selectByExample(example);
        Assertions.assertTrue(cleanUpVersions.isEmpty());
        Map<String, Object> cleanConfig = projectApplicationService.get(request, List.of(ProjectApplicationType.VERSION.VERSION_ENABLE.name()));
        Assertions.assertFalse(cleanConfig.containsKey(ProjectApplicationType.VERSION.VERSION_ENABLE.name()));
    }
}
