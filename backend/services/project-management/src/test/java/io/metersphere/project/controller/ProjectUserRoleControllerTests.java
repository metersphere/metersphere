package io.metersphere.project.controller;

import io.metersphere.project.dto.ProjectUserRoleDTO;
import io.metersphere.project.request.ProjectUserRoleEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberEditRequest;
import io.metersphere.project.request.ProjectUserRoleMemberRequest;
import io.metersphere.project.request.ProjectUserRoleRequest;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.request.OrganizationUserRoleEditRequest;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseUserRolePermissionService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.NO_GLOBAL_USER_ROLE_PERMISSION;
import static io.metersphere.system.controller.result.SystemResultCode.NO_PROJECT_USER_ROLE_PERMISSION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectUserRoleControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private BaseUserRolePermissionService baseUserRolePermissionService;

    public static final String PROJECT_USER_ROLE_LIST = "/user/role/project/list";
    public static final String PROJECT_USER_ROLE_ADD = "/user/role/project/add";
    public static final String PROJECT_USER_ROLE_UPDATE = "/user/role/project/update";
    public static final String PROJECT_USER_ROLE_DELETE = "/user/role/project/delete";
    public static final String PROJECT_USER_ROLE_PERMISSION_SETTING = "/user/role/project/permission/setting";
    public static final String PROJECT_USER_ROLE_PERMISSION_UPDATE = "/user/role/project/permission/update";
    public static final String PROJECT_USER_ROLE_GET_MEMBER_OPTION = "/user/role/project/get-member/option";
    public static final String PROJECT_USER_ROLE_LIST_MEMBER = "/user/role/project/list-member";
    public static final String PROJECT_USER_ROLE_ADD_MEMBER = "/user/role/project/add-member";
    public static final String PROJECT_USER_ROLE_REMOVE_MEMBER = "/user/role/project/remove-member";
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;

    @Test
    @Order(0)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/dml/init_project_user_role.sql")
    public void testProjectUserRoleListSuccess() throws Exception {
        ProjectUserRoleRequest request = new ProjectUserRoleRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("default-project-2");
        request.setKeyword("default-pro-role-3");
        MvcResult mvcResult = this.responsePost(PROJECT_USER_ROLE_LIST, request);
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
        // 返回值中取出第一条数据, 并判断是否包含关键字default
        ProjectUserRoleDTO projectUserRoleDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), ProjectUserRoleDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(projectUserRoleDTO.getName(), request.getKeyword())
                || StringUtils.contains(projectUserRoleDTO.getId(), request.getKeyword()));
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_READ, PROJECT_USER_ROLE_LIST, request);

        // 覆盖用户组没有成员的情况
        request.setProjectId("default-project-2");
        request.setKeyword("");
        this.requestPost(PROJECT_USER_ROLE_LIST, request);
        // 覆盖空数据
        request.setProjectId("default-project-3");
        request.initKeyword("project_member");
        this.requestPost(PROJECT_USER_ROLE_LIST, request);
        request.initKeyword("not_exit");
        this.requestPost(PROJECT_USER_ROLE_LIST, request);
    }

    @Test
    @Order(1)
    public void testProjectUserRoleAddSuccess() throws Exception {
        ProjectUserRoleEditRequest request = new ProjectUserRoleEditRequest();
        request.setName("default-pro-role-5");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_ADD, request);
        // 验证是否添加成功
        ProjectUserRoleRequest roleRequest = new ProjectUserRoleRequest();
        roleRequest.setCurrent(1);
        roleRequest.setPageSize(10);
        roleRequest.setProjectId("default-project-2");
        roleRequest.setKeyword("default-pro-role-5");
        MvcResult mvcResult = this.responsePost(PROJECT_USER_ROLE_LIST, roleRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空, 取第一条是否包含关键字
        Assertions.assertNotNull(pageData);
        ProjectUserRoleDTO roleDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), ProjectUserRoleDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(roleDTO.getName(), roleRequest.getKeyword())
                || StringUtils.contains(roleDTO.getId(), roleRequest.getKeyword()));
        // 权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_ADD, PROJECT_USER_ROLE_ADD, request);
    }

    @Test
    @Order(2)
    public void testProjectUserRoleAddError() throws Exception {
        ProjectUserRoleEditRequest request = new ProjectUserRoleEditRequest();
        // 同名用户组已存在
        request.setName("default-pro-role-2");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_ADD, request, status().is5xxServerError());
    }

    @Test
    @Order(3)
    public void testProjectUserRoleUpdateError() throws Exception {
        ProjectUserRoleEditRequest request = new ProjectUserRoleEditRequest();
        // 用户组不存在
        request.setId("default-pro-role-id-10");
        request.setName("default-pro-role-x");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_UPDATE, request, status().is5xxServerError());
        // 非项目下用户组异常
        request = new ProjectUserRoleEditRequest();
        request.setId(InternalUserRole.ADMIN.getValue());
        request.setName("default-pro-role-x");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_UPDATE, request).andExpect(jsonPath("$.code").value(NO_PROJECT_USER_ROLE_PERMISSION.getCode()));
        // 非全局用户组异常
        request = new ProjectUserRoleEditRequest();
        request.setId(InternalUserRole.PROJECT_ADMIN.getValue());
        request.setName("default-pro-role-x");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_UPDATE, request).andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
        // 用户组名称已存在
        request = new ProjectUserRoleEditRequest();
        request.setId("default-pro-role-id-2");
        request.setName("项目管理员");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_UPDATE, request, status().is5xxServerError());
    }

    @Test
    @Order(4)
    public void testProjectUserRoleUpdateSuccess() throws Exception {
        OrganizationUserRoleEditRequest request = new OrganizationUserRoleEditRequest();
        request.setId("default-pro-role-id-2");
        request.setName("default-pro-role-x");
        request.setScopeId("default-project-2");
        this.requestPost(PROJECT_USER_ROLE_UPDATE, request, status().isOk());
        // 验证是否修改成功
        ProjectUserRoleRequest roleRequest = new ProjectUserRoleRequest();
        roleRequest.setCurrent(1);
        roleRequest.setPageSize(10);
        roleRequest.setProjectId("default-project-2");
        roleRequest.setKeyword("default-pro-role-x");
        MvcResult mvcResult = this.responsePost(PROJECT_USER_ROLE_LIST, roleRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        ProjectUserRoleDTO roleDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), ProjectUserRoleDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(roleDTO.getName(), roleRequest.getKeyword())
                || StringUtils.contains(roleDTO.getId(), roleRequest.getKeyword()));
        // 权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_UPDATE, PROJECT_USER_ROLE_UPDATE, request);
    }

    @Test
    @Order(5)
    public void testProjectUserRoleDeleteError() throws Exception {
        // 用户组不存在
        this.requestGet(PROJECT_USER_ROLE_DELETE + "/default-pro-role-id-10", status().is5xxServerError());
        // 非项目下用户组异常
        this.requestGet(PROJECT_USER_ROLE_DELETE + "/" + InternalUserRole.ADMIN.getValue()).andExpect(jsonPath("$.code").value(NO_PROJECT_USER_ROLE_PERMISSION.getCode()));
        // 非内置用户组异常
        this.requestGet(PROJECT_USER_ROLE_DELETE + "/" + InternalUserRole.PROJECT_ADMIN.getValue()).andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(6)
    public void testProjectUserRoleDeleteSuccess() throws Exception {
        this.requestGet(PROJECT_USER_ROLE_DELETE + "/default-pro-role-id-2", status().isOk());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_GROUP_DELETE, PROJECT_USER_ROLE_DELETE + "/default-pro-role-id-2");
    }

    @Test
    @Order(7)
    public void testProjectUserRolePermissionSettingSuccess() throws Exception {
        MvcResult mvcResult = this.responseGet(PROJECT_USER_ROLE_PERMISSION_SETTING + "/default-pro-role-id-3");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否为init_project_user_role.sql中的数据总数
        Assertions.assertEquals(2, JSON.parseArray(JSON.toJSONString(resultHolder.getData())).size());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_GROUP_READ, PROJECT_USER_ROLE_PERMISSION_SETTING + "/default-pro-role-id-3");
    }

    @Test
    @Order(8)
    public void testProjectUserRolePermissionSettingError() throws Exception {
        // 用户组不存在
        this.requestGet(PROJECT_USER_ROLE_PERMISSION_SETTING + "/default-pro-role-id-10", status().is5xxServerError());
        // 非项目下用户组异常
        this.requestGet(PROJECT_USER_ROLE_PERMISSION_SETTING + "/" + InternalUserRole.ADMIN.getValue())
                .andExpect(jsonPath("$.code").value(NO_PROJECT_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(9)
    public void testProjectUserRolePermissionUpdateSuccess() throws Exception {
        PermissionSettingUpdateRequest request = getPermissionSettingUpdateRequest();
        request.setUserRoleId("default-pro-role-id-3");
        this.requestPost(PROJECT_USER_ROLE_PERMISSION_UPDATE, request, status().isOk());
        // 返回权限勾选PROJECT_GROUP:ADD
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(request.getUserRoleId());
        Set<String> requestPermissionIds = request.getPermissions().stream()
                .filter(PermissionSettingUpdateRequest.PermissionUpdateRequest::getEnable)
                .map(PermissionSettingUpdateRequest.PermissionUpdateRequest::getId)
                .collect(Collectors.toSet());
        // 校验请求成功数据
        Assertions.assertEquals(requestPermissionIds, permissionIds);
        // 权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_UPDATE, PROJECT_USER_ROLE_PERMISSION_UPDATE, request);
    }

    @Test
    @Order(10)
    public void testProjectUserRolePermissionUpdateError() throws Exception {
        // 用户组不存在
        PermissionSettingUpdateRequest request = getPermissionSettingUpdateRequest();
        request.setUserRoleId("default-pro-role-id-10");
        this.requestPost(PROJECT_USER_ROLE_PERMISSION_UPDATE, request, status().is5xxServerError());
        // 非项目下用户组异常
        request.setUserRoleId(InternalUserRole.ADMIN.getValue());
        this.requestPost(PROJECT_USER_ROLE_PERMISSION_UPDATE, request)
                .andExpect(jsonPath("$.code").value(NO_PROJECT_USER_ROLE_PERMISSION.getCode()));
        // 全局用户组异常
        request.setUserRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
        this.requestPost(PROJECT_USER_ROLE_PERMISSION_UPDATE, request)
                .andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(11)
    public void testProjectUserRoleListMemberSuccess() throws Exception {
        ProjectUserRoleMemberRequest request = new ProjectUserRoleMemberRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-3");
        request.setKeyword("admin");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.responsePost(PROJECT_USER_ROLE_LIST_MEMBER, request);
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
        // 返回值中取出第一条数据, 并判断是否包含关键字
        List<User> userList = JSON.parseArray(JSON.toJSONString(pageData.getList()), User.class);
        if (CollectionUtils.isNotEmpty(userList)) {
            User user = userList.getFirst();
            Assertions.assertTrue(StringUtils.contains(user.getName(), request.getKeyword())
                    || StringUtils.contains(user.getId(), request.getKeyword()));
        }

        // email搜索
        request.setKeyword("admin@");
        this.requestPost(PROJECT_USER_ROLE_LIST_MEMBER, request).andExpect(status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_READ, PROJECT_USER_ROLE_LIST_MEMBER, request);
    }

    @Test
    @Order(12)
    public void testProjectUserRoleListMemberError() throws Exception {
        ProjectUserRoleMemberRequest request = new ProjectUserRoleMemberRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-3");
        request.setCurrent(0);
        request.setPageSize(10);
        // 页码有误
        this.requestPost(PROJECT_USER_ROLE_LIST_MEMBER, request, status().isBadRequest());
        request = new ProjectUserRoleMemberRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-3");
        request.setCurrent(1);
        request.setPageSize(1);
        // 页数有误
        this.requestPost(PROJECT_USER_ROLE_LIST_MEMBER, request, status().isBadRequest());
    }

    @Test
    @Order(13)
    public void testProjectUserRoleAddMemberSuccess() throws Exception {
        ProjectUserRoleMemberEditRequest request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-3");
        request.setUserIds(List.of("admin"));
        this.requestPost(PROJECT_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_UPDATE, PROJECT_USER_ROLE_ADD_MEMBER, request);
    }

    @Test
    @Order(14)
    public void testProjectUserRoleAddMemberError() throws Exception {
        ProjectUserRoleMemberEditRequest request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserIds(List.of("admin-x"));
        request.setUserRoleId("default-pro-role-id-3");
        // 用户不存在
        this.requestPost(PROJECT_USER_ROLE_ADD_MEMBER, request, status().is5xxServerError());
        request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserIds(List.of("admin"));
        request.setUserRoleId("default-pro-role-id-x");
        // 用户组不存在
        this.requestPost(PROJECT_USER_ROLE_ADD_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(15)
    public void testProjectUserRoleGetMemberOption() throws Exception {
        // 组织下存在已删除用户
        this.responseGet(PROJECT_USER_ROLE_GET_MEMBER_OPTION + "/default-project-2/default-pro-role-id-4");
        // 组织下用户都已删除
        this.responseGet(PROJECT_USER_ROLE_GET_MEMBER_OPTION + "/default-project-4/default-pro-role-id-3");
        // 组织下无用户
        this.responseGet(PROJECT_USER_ROLE_GET_MEMBER_OPTION + "/default-project-3/default-pro-role-id-3");
    }

    @Test
    @Order(16)
    public void testProjectUserRoleRemoveMemberSuccess() throws Exception {
        ProjectUserRoleMemberEditRequest request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-4");
        request.setUserIds(List.of("admin"));
        this.requestPost(PROJECT_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 成员项目用户组存在多个, 移除成功
        this.requestPost(PROJECT_USER_ROLE_REMOVE_MEMBER, request, status().isOk());
        // 权限校验
        request.setProjectId(DEFAULT_PROJECT_ID);
        requestPostPermissionTest(PermissionConstants.PROJECT_GROUP_UPDATE, PROJECT_USER_ROLE_REMOVE_MEMBER, request);
    }

    @Test
    @Order(17)
    public void testProjectUserRoleRemoveMemberError() throws Exception {
        ProjectUserRoleMemberEditRequest request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserIds(List.of("admin-x"));
        request.setUserRoleId("default-pro-role-id-3");
        // 用户不存在
        this.requestPost(PROJECT_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserIds(List.of("admin"));
        request.setUserRoleId("default-pro-role-id-x");
        // 用户组不存在
        this.requestPost(PROJECT_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-3");
        request.setUserIds(List.of("admin"));
        // 成员用户组只有一个, 移除失败
        this.requestPost(PROJECT_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        //移除最后一个管理员移除失败
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelation.setCreateUser("admin");
        userRoleRelation.setRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
        userRoleRelation.setUserId("admin");
        userRoleRelation.setSourceId("default-project-2");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setOrganizationId("default-project-2");
        userRoleRelationMapper.insert(userRoleRelation);
        request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId(InternalUserRole.PROJECT_ADMIN.getValue());
        request.setUserIds(List.of("admin"));
        // 成员用户组只有一个, 移除失败
        this.requestPost(PROJECT_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());

    }

    @Test
    @Order(18)
    public void testProjectUserRoleDeleteOnlyMemberSuccess() throws Exception {
        ProjectUserRoleMemberEditRequest request = new ProjectUserRoleMemberEditRequest();
        request.setProjectId("default-project-2");
        request.setUserRoleId("default-pro-role-id-4");
        request.setUserIds(List.of("default-pro-admin-user"));
        this.requestPost(PROJECT_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 移除用户组, 且存在成员仅有该用户组
        this.requestGet(PROJECT_USER_ROLE_DELETE + "/default-pro-role-id-3", status().isOk());
    }

    private PermissionSettingUpdateRequest getPermissionSettingUpdateRequest() {
        PermissionSettingUpdateRequest request = new PermissionSettingUpdateRequest();
        request.setPermissions(new ArrayList<>() {
            @Serial
            private static final long serialVersionUID = -1719021806631967745L;

            {
                // 取消PROJECT_GROUP:READ权限
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("PROJECT_GROUP:READ", false));
                // 添加PROJECT_GROUP:ADD, PROJECT_GROUP:UPDATE权限
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("PROJECT_GROUP:READ+ADD", true));
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("PROJECT_GROUP:READ+UPDATE", true));
            }
        });
        return request;
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

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
