package io.metersphere.system.controller;

import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.request.OrganizationUserRoleEditRequest;
import io.metersphere.system.dto.request.OrganizationUserRoleMemberEditRequest;
import io.metersphere.system.dto.request.OrganizationUserRoleMemberRequest;
import io.metersphere.system.dto.sdk.request.PermissionSettingUpdateRequest;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.BaseUserRolePermissionService;
import io.metersphere.system.service.OrganizationService;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.NO_GLOBAL_USER_ROLE_PERMISSION;
import static io.metersphere.system.controller.result.SystemResultCode.NO_ORG_USER_ROLE_PERMISSION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationUserRoleControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private BaseUserRolePermissionService baseUserRolePermissionService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;

    public static final String ORGANIZATION_USER_ROLE_LIST = "/user/role/organization/list";
    public static final String ORGANIZATION_USER_ROLE_ADD = "/user/role/organization/add";
    public static final String ORGANIZATION_USER_ROLE_UPDATE = "/user/role/organization/update";
    public static final String ORGANIZATION_USER_ROLE_DELETE = "/user/role/organization/delete";
    public static final String ORGANIZATION_USER_ROLE_PERMISSION_SETTING = "/user/role/organization/permission/setting";
    public static final String ORGANIZATION_USER_ROLE_PERMISSION_UPDATE = "/user/role/organization/permission/update";
    public static final String ORGANIZATION_USER_ROLE_GET_MEMBER_OPTION = "/user/role/organization/get-member/option";
    public static final String ORGANIZATION_USER_ROLE_LIST_MEMBER = "/user/role/organization/list-member";
    public static final String ORGANIZATION_USER_ROLE_ADD_MEMBER = "/user/role/organization/add-member";
    public static final String ORGANIZATION_USER_ROLE_REMOVE_MEMBER = "/user/role/organization/remove-member";

    @Test
    @Order(0)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "/dml/init_organization_user_role.sql")
    public void testOrganizationUserRoleListSuccess() throws Exception {
        String organizationId = "default-organization-2";
        MvcResult mvcResult = this.responseGet(ORGANIZATION_USER_ROLE_LIST + "/" + organizationId);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否为init_organization_user_role.sql中的数据总数
        Assertions.assertFalse(JSON.parseArray(JSON.toJSONString(resultHolder.getData())).isEmpty());
        // 权限校验
        OrganizationDTO defaultOrganization = getDefault();
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ, ORGANIZATION_USER_ROLE_LIST + "/" + defaultOrganization.getId());
    }

    @Test
    @Order(1)
    public void testOrganizationUserRoleAddSuccess() throws Exception {
        OrganizationUserRoleEditRequest request = new OrganizationUserRoleEditRequest();
        request.setName("default-org-role-5");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_ADD, request);
        // 验证是否添加成功
        String organizationId = "default-organization-2";
        MvcResult mvcResult = this.responseGet(ORGANIZATION_USER_ROLE_LIST + "/" + organizationId);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否为init_organization_user_role.sql中的数据总数
        Assertions.assertFalse(JSON.parseArray(JSON.toJSONString(resultHolder.getData())).isEmpty());
        // 权限校验
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_ADD, ORGANIZATION_USER_ROLE_ADD, request);
    }

    @Test
    @Order(2)
    public void testOrganizationUserRoleAddError() throws Exception {
        OrganizationUserRoleEditRequest request = new OrganizationUserRoleEditRequest();
        // 同名用户组已存在
        request.setName("default-org-role-2");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_ADD, request, status().is5xxServerError());
    }

    @Test
    @Order(3)
    public void testOrganizationUserRoleUpdateError() throws Exception {
        OrganizationUserRoleEditRequest request = new OrganizationUserRoleEditRequest();
        // 用户组不存在
        request.setId("default-org-role-id-10");
        request.setName("default-org-role-x");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_UPDATE, request, status().is5xxServerError());
        // 非组织下用户组异常
        request = new OrganizationUserRoleEditRequest();
        request.setId(InternalUserRole.ADMIN.getValue());
        request.setName("default-org-role-x");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_UPDATE, request).andExpect(jsonPath("$.code").value(NO_ORG_USER_ROLE_PERMISSION.getCode()));
        // 非内置用户组异常
        request = new OrganizationUserRoleEditRequest();
        request.setId(InternalUserRole.ORG_ADMIN.getValue());
        request.setName("default-org-role-x");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_UPDATE, request).andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
        // 用户组名称已存在
        request = new OrganizationUserRoleEditRequest();
        request.setId("default-org-role-id-2");
        request.setName("组织管理员");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_UPDATE, request, status().is5xxServerError());
    }

    @Test
    @Order(4)
    public void testOrganizationUserRoleUpdateSuccess() throws Exception {
        OrganizationUserRoleEditRequest request = new OrganizationUserRoleEditRequest();
        request.setId("default-org-role-id-2");
        request.setName("default-org-role-x");
        request.setScopeId("default-organization-2");
        this.requestPost(ORGANIZATION_USER_ROLE_UPDATE, request, status().isOk());
        // 验证是否修改成功
        String organizationId = "default-organization-2";
        MvcResult mvcResult = this.responseGet(ORGANIZATION_USER_ROLE_LIST + "/" + organizationId);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否包含修改后的数据
        List<UserRole> userRoles = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), UserRole.class);
        Assertions.assertTrue(userRoles.stream().anyMatch(userRole -> "default-org-role-x".equals(userRole.getName())));
        // 权限校验
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE, ORGANIZATION_USER_ROLE_UPDATE, request);
    }

    @Test
    @Order(5)
    public void testOrganizationUserRoleDeleteError() throws Exception {
        // 用户组不存在
        this.requestGet(ORGANIZATION_USER_ROLE_DELETE + "/default-org-role-id-10", status().is5xxServerError());
        // 非组织下用户组异常
        this.requestGet(ORGANIZATION_USER_ROLE_DELETE + "/" + InternalUserRole.ADMIN.getValue()).andExpect(jsonPath("$.code").value(NO_ORG_USER_ROLE_PERMISSION.getCode()));
        // 非内置用户组异常
        this.requestGet(ORGANIZATION_USER_ROLE_DELETE + "/" + InternalUserRole.ORG_ADMIN.getValue()).andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(6)
    public void testOrganizationUserRoleDeleteSuccess() throws Exception {
        this.requestGet(ORGANIZATION_USER_ROLE_DELETE + "/default-org-role-id-2", status().isOk());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_DELETE, ORGANIZATION_USER_ROLE_DELETE + "/default-org-role-id-2");
    }

    @Test
    @Order(7)
    public void testOrganizationUserRolePermissionSettingSuccess() throws Exception {
        MvcResult mvcResult = this.responseGet(ORGANIZATION_USER_ROLE_PERMISSION_SETTING + "/default-org-role-id-3");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否为init_organization_user_role.sql中的数据总数
        Assertions.assertEquals(1, JSON.parseArray(JSON.toJSONString(resultHolder.getData())).size());
        // 权限校验
        requestGetPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ, ORGANIZATION_USER_ROLE_PERMISSION_SETTING + "/default-org-role-id-3");
    }

    @Test
    @Order(8)
    public void testOrganizationUserRolePermissionSettingError() throws Exception {
        // 用户组不存在
        this.requestGet(ORGANIZATION_USER_ROLE_PERMISSION_SETTING + "/default-org-role-id-10", status().is5xxServerError());
        // 非组织下用户组异常
        this.requestGet(ORGANIZATION_USER_ROLE_PERMISSION_SETTING + "/" + InternalUserRole.ADMIN.getValue())
                .andExpect(jsonPath("$.code").value(NO_ORG_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(9)
    public void testOrganizationUserRolePermissionUpdateSuccess() throws Exception {
        PermissionSettingUpdateRequest request = getPermissionSettingUpdateRequest();
        request.setUserRoleId("default-org-role-id-3");
        this.requestPost(ORGANIZATION_USER_ROLE_PERMISSION_UPDATE, request, status().isOk());
        // 返回权限勾选ORGANIZATION_USER_ROLE:CREATE
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(request.getUserRoleId());
        Set<String> requestPermissionIds = request.getPermissions().stream()
                .filter(PermissionSettingUpdateRequest.PermissionUpdateRequest::getEnable)
                .map(PermissionSettingUpdateRequest.PermissionUpdateRequest::getId)
                .collect(Collectors.toSet());
        // 校验请求成功数据
        Assertions.assertEquals(requestPermissionIds, permissionIds);
        // 权限校验
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE, ORGANIZATION_USER_ROLE_PERMISSION_UPDATE, request);
    }

    @Test
    @Order(10)
    public void testOrganizationUserRolePermissionUpdateError() throws Exception {
        // 用户组不存在
        PermissionSettingUpdateRequest request = getPermissionSettingUpdateRequest();
        request.setUserRoleId("default-org-role-id-10");
        this.requestPost(ORGANIZATION_USER_ROLE_PERMISSION_UPDATE, request, status().is5xxServerError());
        // 非组织下用户组异常
        request.setUserRoleId(InternalUserRole.ADMIN.getValue());
        this.requestPost(ORGANIZATION_USER_ROLE_PERMISSION_UPDATE, request)
                .andExpect(jsonPath("$.code").value(NO_ORG_USER_ROLE_PERMISSION.getCode()));
        // 内置用户组异常
        request.setUserRoleId(InternalUserRole.ORG_ADMIN.getValue());
        this.requestPost(ORGANIZATION_USER_ROLE_PERMISSION_UPDATE, request)
                .andExpect(jsonPath("$.code").value(NO_GLOBAL_USER_ROLE_PERMISSION.getCode()));
    }

    @Test
    @Order(11)
    public void testOrganizationUserRoleListMemberSuccess() throws Exception {
        OrganizationUserRoleMemberRequest request = new OrganizationUserRoleMemberRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-3");
        request.setKeyword("admin");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.responsePost(ORGANIZATION_USER_ROLE_LIST_MEMBER, request);
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
        if(CollectionUtils.isNotEmpty(userList)) {
            User user = userList.getFirst();
            Assertions.assertTrue(StringUtils.contains(user.getName(), request.getKeyword())
                    || StringUtils.contains(user.getId(), request.getKeyword()));
        }
        // email搜索
        request.setKeyword("admin@");
        this.requestPost(ORGANIZATION_USER_ROLE_LIST_MEMBER, request).andExpect(status().isOk());

        // 权限校验
        request.setOrganizationId(getDefault().getId());
        requestPostPermissionsTest(List.of(PermissionConstants.ORGANIZATION_USER_ROLE_READ, PermissionConstants.ORGANIZATION_MEMBER_READ), ORGANIZATION_USER_ROLE_LIST_MEMBER, request);
    }

    @Test
    @Order(12)
    public void testOrganizationUserRoleListMemberError() throws Exception {
        OrganizationUserRoleMemberRequest request = new OrganizationUserRoleMemberRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-3");
        request.setCurrent(0);
        request.setPageSize(10);
        // 页码有误
        this.requestPost(ORGANIZATION_USER_ROLE_LIST_MEMBER, request, status().isBadRequest());
        request = new OrganizationUserRoleMemberRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-3");
        request.setCurrent(1);
        request.setPageSize(1);
        // 页数有误
        this.requestPost(ORGANIZATION_USER_ROLE_LIST_MEMBER, request, status().isBadRequest());
    }

    @Test
    @Order(13)
    public void testOrganizationUserRoleAddMemberSuccess() throws Exception {
        OrganizationUserRoleMemberEditRequest request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-3");
        request.setUserIds(List.of("admin"));
        this.requestPost(ORGANIZATION_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 权限校验
        request.setOrganizationId(getDefault().getId());
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE, ORGANIZATION_USER_ROLE_ADD_MEMBER, request);
    }

    @Test
    @Order(14)
    public void testOrganizationUserRoleAddMemberError() throws Exception {
        OrganizationUserRoleMemberEditRequest request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserIds(List.of("admin-x"));
        request.setUserRoleId("default-org-role-id-3");
        // 用户不存在
        this.requestPost(ORGANIZATION_USER_ROLE_ADD_MEMBER, request, status().is5xxServerError());
        request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserIds(List.of("admin"));
        request.setUserRoleId("default-org-role-id-x");
        // 用户组不存在
        this.requestPost(ORGANIZATION_USER_ROLE_ADD_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(15)
    public void testOrganizationUserRoleGetMemberOption() throws Exception {
        // 组织下存在已删除用户
        this.responseGet(ORGANIZATION_USER_ROLE_GET_MEMBER_OPTION + "/default-organization-2/default-org-role-id-4");
        // 组织下用户都已删除
        this.responseGet(ORGANIZATION_USER_ROLE_GET_MEMBER_OPTION + "/default-organization-4/default-org-role-id-3");
        // 组织下无用户
        this.responseGet(ORGANIZATION_USER_ROLE_GET_MEMBER_OPTION + "/default-organization-3/default-org-role-id-3");
    }

    @Test
    @Order(16)
    public void testOrganizationUserRoleRemoveMemberSuccess() throws Exception {
        OrganizationUserRoleMemberEditRequest request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-4");
        request.setUserIds(List.of("admin"));
        this.requestPost(ORGANIZATION_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 成员组织用户组存在多个, 移除成功
        this.requestPost(ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request, status().isOk());
        // 权限校验
        request.setOrganizationId(getDefault().getId());
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_USER_ROLE_READ_UPDATE, ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request);
    }

    @Test
    @Order(17)
    public void testOrganizationUserRoleRemoveMemberError() throws Exception {
        OrganizationUserRoleMemberEditRequest request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserIds(List.of("admin-x"));
        request.setUserRoleId("default-org-role-id-3");
        // 用户不存在
        this.requestPost(ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserIds(List.of("admin"));
        request.setUserRoleId("default-org-role-id-x");
        // 用户组不存在
        this.requestPost(ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-3");
        request.setUserIds(List.of("admin"));
        // 成员用户组只有一个, 移除失败
        this.requestPost(ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
        //移除最后一个管理员移除失败
        UserRoleRelation userRoleRelation = new UserRoleRelation();
        userRoleRelation.setId(IDGenerator.nextStr());
        userRoleRelation.setCreateUser("admin");
        userRoleRelation.setRoleId(InternalUserRole.ORG_ADMIN.getValue());
        userRoleRelation.setUserId("admin");
        userRoleRelation.setSourceId("default-organization-2");
        userRoleRelation.setCreateTime(System.currentTimeMillis());
        userRoleRelation.setOrganizationId("default-organization-2");
        userRoleRelationMapper.insert(userRoleRelation);
        request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId(InternalUserRole.ORG_ADMIN.getValue());
        request.setUserIds(List.of("admin"));
        // 成员用户组只有一个, 移除失败
        this.requestPost(ORGANIZATION_USER_ROLE_REMOVE_MEMBER, request, status().is5xxServerError());
    }

    @Test
    @Order(18)
    public void testOrganizationUserRoleDeleteOnlyMemberSuccess() throws Exception {
        OrganizationUserRoleMemberEditRequest request = new OrganizationUserRoleMemberEditRequest();
        request.setOrganizationId("default-organization-2");
        request.setUserRoleId("default-org-role-id-4");
        request.setUserIds(List.of("default-admin-user"));
        this.requestPost(ORGANIZATION_USER_ROLE_ADD_MEMBER, request, status().isOk());
        // 移除用户组, 且存在成员仅有该用户组
        this.requestGet(ORGANIZATION_USER_ROLE_DELETE + "/default-org-role-id-3", status().isOk());
    }

    private PermissionSettingUpdateRequest getPermissionSettingUpdateRequest(){
        PermissionSettingUpdateRequest request = new PermissionSettingUpdateRequest();
        request.setPermissions(new ArrayList<>() {
            {
                // 取消ORGANIZATION_USER_ROLE:READ权限
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("ORGANIZATION_USER_ROLE:READ", false));
                // 添加ORGANIZATION_USER_ROLE:CREATE, ORGANIZATION_USER_ROLE:UPDATE权限
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("ORGANIZATION_USER_ROLE:READ+ADD", true));
                add(new PermissionSettingUpdateRequest.PermissionUpdateRequest("ORGANIZATION_USER_ROLE:READ+UPDATE", true));
            }
        });
        return request;
    }

    private OrganizationDTO getDefault() {
        return organizationService.getDefault();
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
