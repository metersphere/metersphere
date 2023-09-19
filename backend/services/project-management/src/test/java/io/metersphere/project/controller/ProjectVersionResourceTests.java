package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.domain.ProjectVersionExample;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectVersionResourceTests extends BaseTest {

    private final ProjectServiceInvoker serviceInvoker;

    public static final String TEST_PROJECT_ID = "project-version-test-id";

    @Resource
    private ProjectVersionMapper projectVersionMapper;

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
        // 项目资源初始化版本, 默认为一条版本记录
        serviceInvoker.invokeCreateServices(TEST_PROJECT_ID);
        List<ProjectVersion> createVersions = projectVersionMapper.selectByExample(example);
        Assertions.assertEquals(1, createVersions.size());
        // 项目资源清理版本, 为空
        serviceInvoker.invokeServices(TEST_PROJECT_ID);
        List<ProjectVersion> cleanUpVersions = projectVersionMapper.selectByExample(example);
        Assertions.assertTrue(cleanUpVersions.isEmpty());
    }
}
