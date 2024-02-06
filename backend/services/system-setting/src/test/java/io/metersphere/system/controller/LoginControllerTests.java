package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private SystemProjectService systemProjectService;

    private final String login = "/login";

    @Test
    @Sql(scripts = {"/dml/init_user_login_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testLogin() throws Exception {
        // 系统管理员
        AddProjectRequest project = new AddProjectRequest();
        project.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        project.setName("test-login-projectName");
        project.setEnable(true);
        project.setUserIds(List.of("test.login"));
        ProjectDTO add = systemProjectService.add(project, "test.login");


        // 1. 正常登录  管理员有项目权限
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login1", "test.login1@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 2. 正常登录  项目id和组织id不匹配
        User user = new User();
        user.setLastOrganizationId("test.login1");
        user.setId("test.login1");
        user.setLastProjectId(add.getId());
        userMapper.updateByPrimaryKeySelective(user);
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login1", "test.login1@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 3. 正常登录  last项目没有权限 但是有工作空间权限
        user.setLastProjectId("no_such_project");
        userMapper.updateByPrimaryKeySelective(user);
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login1", "test.login1@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 非系统管理员
        // 1. last组织id没有权限
        user.setId("test.login");
        user.setLastOrganizationId("no_such_organization");
        user.setLastProjectId("no_such_project");
        userMapper.updateByPrimaryKeySelective(user);
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login", "test.login@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // 2. last组织下没有项目
        user.setLastOrganizationId("test-login-organizationId");
        user.setLastProjectId(null);
        userMapper.updateByPrimaryKeySelective(user);
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId("test.login");
        userRoleRelation.setOrganizationId("test-login-organizationId");
        userRoleRelation.setSourceId("test-login-organizationId");
        userRoleRelation.setRoleId("org-member");
        userRoleRelation.setCreateUser("test.login");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelationMapper.insert(userRoleRelation);
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login", "test.login@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // 3. last组织下有项目 但是没有组织权限
        user.setLastOrganizationId(DEFAULT_ORGANIZATION_ID);
        user.setLastProjectId(null);
        userMapper.updateByPrimaryKeySelective(user);
        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login", "test.login@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // 4. last组织下有项目 有组织权限 项目随机取一个
        userRoleRelationMapper.deleteByPrimaryKey(userRoleRelation.getId());
        userRoleRelation = new UserRoleRelation();
        userRoleRelation.setUserId("test.login");
        userRoleRelation.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        userRoleRelation.setSourceId(DEFAULT_ORGANIZATION_ID);
        userRoleRelation.setRoleId("org-member");
        userRoleRelation.setCreateUser("test.login");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelationMapper.insert(userRoleRelation);
        user.setLastOrganizationId(DEFAULT_ORGANIZATION_ID);
        user.setLastProjectId(null);
        userMapper.updateByPrimaryKeySelective(user);

        mockMvc.perform(MockMvcRequestBuilders.post(login)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test.login", "test.login@163.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


    }


}
