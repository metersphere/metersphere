package io.metersphere.project.controller;

import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.request.*;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.request.UserInviteRequest;
import io.metersphere.system.utils.Pager;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectMemberControllerTests extends BaseTest {

    public static final String LIST_MEMBER = "/project/member/list";
    public static final String GET_MEMBER = "/project/member/get-member/option";
    public static final String GET_ROLE = "/project/member/get-role/option";
    public static final String ADD_MEMBER = "/project/member/add";
    public static final String INVITE = "/project/member/invite";
    public static final String UPDATE_MEMBER = "/project/member/update";
    public static final String REMOVE_MEMBER = "/project/member/remove";
    public static final String ADD_ROLE = "/project/member/add-role";
    public static final String BATCH_REMOVE_MEMBER = "/project/member/batch/remove";
    public static final String COMMENT_USER_OPTION = "/project/member/comment/user-option";
    public static final String UPDATE_MEMBER_ROLE = "/project/member/update-member";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_member.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testListMemberSuccess() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest();
        request.setProjectId("default-project-member-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setKeyword("default");
        MvcResult mvcResult = this.responsePost(LIST_MEMBER, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());
        // 返回值中取出第一条数据, 并判断name, email, phone是否包含关键字default
        ProjectUserDTO projectUserDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), ProjectUserDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(projectUserDTO.getName(), request.getKeyword())
                || StringUtils.contains(projectUserDTO.getEmail(), request.getKeyword())
                || StringUtils.contains(projectUserDTO.getPhone(), request.getKeyword()));
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_READ, LIST_MEMBER, request);
    }

    @Test
    @Order(2)
    public void testListMemberEmpty() throws Exception {
        // 空数据覆盖
        ProjectMemberRequest request = new ProjectMemberRequest();
        request.setProjectId("default-project-member-x");
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(LIST_MEMBER, request, status().isOk());
    }

    @Test
    @Order(3)
    public void testListMemberError() throws Exception {
        // 页码有误
        ProjectMemberRequest request = new ProjectMemberRequest();
        request.setCurrent(0);
        request.setPageSize(10);
        this.requestPost(LIST_MEMBER, request, status().isBadRequest());
        // 页数有误
        request = new ProjectMemberRequest();
        request.setCurrent(1);
        request.setPageSize(1);
        this.requestPost(LIST_MEMBER, request, status().isBadRequest());
    }

    @Test
    @Order(4)
    public void testGetMemberOption() throws Exception {
        // 正常数据
        this.requestGet(GET_MEMBER + "/default-project-member-test", status().isOk());
        // 项目不存在
        this.requestGet(GET_MEMBER + "/default-project-member-x", status().isOk());
        // 组织成员为空
        this.requestGet(GET_MEMBER + "/default-project-member-test-1", status().isOk());
        // 项目成员为空
        this.requestGet(GET_MEMBER + "/default-project-member-test-2", status().isOk());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_USER_READ, GET_MEMBER + "/" + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(5)
    public void testGetRoleOption() throws Exception {
        this.requestGet(GET_ROLE + "/default-project-member-test", status().isOk());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_USER_READ, GET_ROLE + "/" + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(6)
    public void testAddMemberSuccess() throws Exception {
        ProjectMemberAddRequest request = new ProjectMemberAddRequest();
        request.setProjectId("default-project-member-test");
        request.setUserIds(List.of("default-project-member-user-1", "default-project-member-user-del"));
        request.setRoleIds(List.of("project_admin", "project_admin_x", "project_member"));
        this.requestPost(ADD_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_ADD, ADD_MEMBER, request);

        //顺便测试邀请
        UserInviteRequest userInviteRequest = new UserInviteRequest();
        userInviteRequest.setInviteEmails(new ArrayList<>(Collections.singletonList("abcde12345@qq.com")));
        userInviteRequest.setUserRoleIds(request.getRoleIds());
        userInviteRequest.setProjectId("default-project-member-test");
        this.requestPost(INVITE, userInviteRequest);
        userInviteRequest.setProjectId("NOT_EXIST_PROJECT_ID_BY_SOMEBODY_J");
        this.requestPost(INVITE, userInviteRequest);
        // 权限校验
        userInviteRequest.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_INVITE, INVITE, userInviteRequest);
    }

    @Test
    @Order(7)
    public void testAddMemberRepeat() throws Exception {
        ProjectMemberAddRequest request = new ProjectMemberAddRequest();
        request.setProjectId("default-project-member-test");
        request.setUserIds(List.of("default-project-member-user-1"));
        request.setRoleIds(List.of("project_admin"));
        this.requestPost(ADD_MEMBER, request, status().isOk());
    }

    @Test
    @Order(8)
    public void testAddMemberError() throws Exception {
        ProjectMemberAddRequest request = new ProjectMemberAddRequest();
        request.setProjectId("default-project-member-x");
        request.setUserIds(List.of("default-project-member-user-1", "default-project-member-user-del"));
        request.setRoleIds(List.of("project_admin", "project_admin_x", "project_member"));
        this.requestPost(ADD_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(9)
    public void testUpdateMemberSuccess() throws Exception {
        // 不存在的用户组
        ProjectMemberEditRequest request = new ProjectMemberEditRequest();
        request.setProjectId("default-project-member-test");
        request.setUserId("default-project-member-user-1");
        request.setRoleIds(List.of("project_admin_x"));
        this.requestPost(UPDATE_MEMBER, request, status().isOk());
        // 存在的用户组
        request.setRoleIds(List.of("project_admin", "project_member"));
        this.requestPost(UPDATE_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_UPDATE, UPDATE_MEMBER, request);
    }

    @Test
    @Order(10)
    public void testUpdateMemberError() throws Exception {
        ProjectMemberEditRequest request = new ProjectMemberEditRequest();
        request.setProjectId("default-project-member-x");
        request.setUserId("default-project-member-user-1");
        request.setRoleIds(List.of("project_admin", "project_admin_x", "project_member"));
        this.requestPost(UPDATE_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(11)
    public void testRemoveMemberSuccess() throws Exception {
        this.requestGet(REMOVE_MEMBER + "/default-project-member-test/default-project-member-user-1", status().isOk());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_USER_DELETE, REMOVE_MEMBER + "/" + DEFAULT_PROJECT_ID + "/default-project-member-user-1");
    }

    @Test
    @Order(12)
    public void testRemoveMemberError() throws Exception {
        this.requestGet(REMOVE_MEMBER + "/default-project-member-x/default-project-member-user-1", status().is5xxServerError());
        this.requestGet(REMOVE_MEMBER + "/default-project-member-test-1/admin", status().is5xxServerError());
    }

    @Test
    @Order(13)
    public void testAddMemberRoleSuccess() throws Exception {
        ProjectMemberAddRoleRequest request = new ProjectMemberAddRoleRequest();
        request.setProjectId("default-project-member-test");
        request.setSelectIds(List.of("default-project-member-user-1", "default-project-member-user-2"));
        request.setRoleIds(List.of("project_admin", "project_member"));
        request.setSelectAll(false);
        this.requestPost(ADD_ROLE, request, status().isOk());
        request.setSelectAll(true);
        request.setSelectIds(List.of());
        this.requestPost(ADD_ROLE, request, status().isOk());
        request.setExcludeIds(List.of("project_admin"));
        this.requestPost(ADD_ROLE, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_UPDATE, ADD_ROLE, request);
    }

    @Test
    @Order(14)
    public void testAddMemberRoleError() throws Exception {
        ProjectMemberAddRoleRequest request = new ProjectMemberAddRoleRequest();
        request.setProjectId("default-project-member-x");
        request.setSelectAll(false);
        request.setSelectIds(List.of("default-project-member-user-1", "default-project-member-user-2"));
        request.setRoleIds(List.of("project_admin", "project_member"));
        this.requestPost(ADD_ROLE, request, status().is5xxServerError());
        request.setProjectId("default-project-member-test");
        request.setSelectIds(List.of());
        this.requestPost(ADD_ROLE, request, status().is5xxServerError());
    }

    @Test
    @Order(15)
    public void testBatchRemoveMemberSuccess() throws Exception {
        ProjectMemberBatchDeleteRequest request = new ProjectMemberBatchDeleteRequest();
        request.setProjectId("default-project-member-test");
        request.setSelectIds(List.of("default-project-member-user-1", "default-project-member-user-2"));
        this.requestPost(BATCH_REMOVE_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_DELETE, BATCH_REMOVE_MEMBER, request);
    }

    @Test
    @Order(16)
    public void testBatchRemoveMember() throws Exception {
        ProjectMemberBatchDeleteRequest request = new ProjectMemberBatchDeleteRequest();
        request.setProjectId("default-project-member-x");
        request.setSelectIds(List.of("default-project-member-user-1", "default-project-member-user-2"));
        this.requestPost(BATCH_REMOVE_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(17)
    public void testGetProjectCommentUserOption() throws Exception {
        this.requestGet(COMMENT_USER_OPTION + "/default-project-member-test", status().isOk());
    }

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private void requestGet(String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(18)
    public void testUpdateMemberRole() throws Exception {
        // 不存在的用户组
        ProjectMemberEditRequest request = new ProjectMemberEditRequest();
        request.setProjectId("default-project-member-test");
        request.setUserId("default-project-member-user-1");
        request.setRoleIds(List.of("project_admin_x"));
        this.requestPost(UPDATE_MEMBER, request, status().isOk());
        // 存在的用户组
        request.setRoleIds(List.of("project_admin", "project_member"));
        this.requestPost(UPDATE_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_USER_UPDATE, UPDATE_MEMBER, request);
    }
}
