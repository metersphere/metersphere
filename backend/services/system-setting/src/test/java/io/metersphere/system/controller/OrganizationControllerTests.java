package io.metersphere.system.controller;

import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationControllerTests extends BaseTest {

    @Resource
    private MockMvc mockMvc;


    public static final String ORGANIZATION_LIST_ADD_MEMBER = "/organization/add-member";
    public static final String ORGANIZATION_USER_INVITE = "/organization/user/invite";
    public static final String ORGANIZATION_UPDATE_MEMBER_TO_ROLE = "/organization/role/update-member";
    public static final String ORGANIZATION_UPDATE_MEMBER = "/organization/update-member";
    public static final String ORGANIZATION_REMOVE_MEMBER = "/organization/remove-member";
    public static final String ORGANIZATION_MEMBER_LIST = "/organization/member/list";
    public static final String ORGANIZATION_PROJECT_ADD_MEMBER = "/organization/project/add-member";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_sys_organization.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addMemberByOrgSuccess() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user", "sys_default_user2"));
        organizationMemberRequest.setUserRoleIds(Arrays.asList("sys_default_org_role_id_3", "sys_default_project_role_id_1"));
        this.requestPost(ORGANIZATION_LIST_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserOne", "sys_default_organization_3", true, "sys_default_org_role_id_3", null, false, null, null);

        //测试邮箱邀请
        UserInviteRequest userInviteRequest = new UserInviteRequest();
        userInviteRequest.setInviteEmails(new ArrayList<>(Collections.singletonList("abcde12345@qq.com")));
        userInviteRequest.setUserRoleIds(organizationMemberRequest.getUserRoleIds());
        userInviteRequest.setOrganizationId(organizationMemberRequest.getOrganizationId());
        this.requestPost(ORGANIZATION_USER_INVITE, userInviteRequest);
        //输入错误的组织ID
        userInviteRequest.setOrganizationId(null);
        this.requestPost(ORGANIZATION_USER_INVITE, userInviteRequest).andExpect(status().is5xxServerError());
        userInviteRequest.setOrganizationId("not_exist_organization_id_by_somebody_J");
        this.requestPost(ORGANIZATION_USER_INVITE, userInviteRequest).andExpect(status().is5xxServerError());

        // 权限校验
        userInviteRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        requestPostPermissionTest(PermissionConstants.ORGANIZATION_MEMBER_INVITE, ORGANIZATION_USER_INVITE, userInviteRequest);
    }

    @Test
    @Order(1)
    public void addMemberByOrgError() throws Exception {
        //组织ID正确
        // 成员选择为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Collections.emptyList(), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 成员都不存在
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Arrays.asList("sys_default_userX", "sys_default_userY"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 用户组为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Arrays.asList("sys_default_user", "sys_default_user2"), Collections.emptyList(), status().isBadRequest());
        // 用户组都不存在
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_X", "sys_default_org_role_id_Y"), status().is5xxServerError());
        // 用户组有一个不存在
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_Y"), status().isOk());
        //成员和用户组都为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "sys_default_organization_3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        // 组织不存在
        // 成员选择为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Collections.emptyList(), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 用户组不存在
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Arrays.asList("sys_default_user", "sys_default_user2"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        //成员和用户组存在
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        //组织为空
        addOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, null, Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().isBadRequest());

    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_sys_org_project.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void updateOrgMemberSuccessWithAll() throws Exception {
        OrganizationMemberUpdateRequest organizationMemberUpdateRequest = new OrganizationMemberUpdateRequest();
        organizationMemberUpdateRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberUpdateRequest.setMemberId("sys_default_user2");
        organizationMemberUpdateRequest.setUserRoleIds(List.of("sys_default_org_role_id_1"));
        organizationMemberUpdateRequest.setProjectIds(List.of("sys_org_projectId"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER, organizationMemberUpdateRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserTwo", "sys_default_organization_3", true, "sys_default_org_role_id_1", "sys_org_projectId", false, null, null);
    }

    @Test
    @Order(3)
    public void updateOrgMemberSuccessWithNoProjectIds() throws Exception {
        OrganizationMemberUpdateRequest organizationMemberUpdateRequest = new OrganizationMemberUpdateRequest();
        organizationMemberUpdateRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberUpdateRequest.setMemberId("sys_default_user2");
        organizationMemberUpdateRequest.setUserRoleIds(List.of("sys_default_org_role_id_4"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER, organizationMemberUpdateRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserTwo", "sys_default_organization_3", true, "sys_default_org_role_id_4", null, false, null, null);
    }

    @Test
    @Order(4)
    public void updateOrgMemberSuccessWithNoSysProjectIds() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_7");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user", "sys_default_user2"));
        organizationMemberRequest.setUserRoleIds(Arrays.asList("sys_default_org_role_id_7", "sys_default_project_role_id_8"));
        this.requestPost(ORGANIZATION_LIST_ADD_MEMBER, organizationMemberRequest, status().isOk());

        OrganizationMemberUpdateRequest organizationMemberUpdateRequest = new OrganizationMemberUpdateRequest();
        organizationMemberUpdateRequest.setOrganizationId("sys_default_organization_7");
        organizationMemberUpdateRequest.setMemberId("sys_default_user2");
        organizationMemberUpdateRequest.setUserRoleIds(List.of("sys_default_org_role_id_7"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER, organizationMemberUpdateRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserTwo", "sys_default_organization_7", true, "sys_default_org_role_id_7", null, false, null, null);
    }

    @Test
    @Order(5)
    public void updateOrgMemberSuccessWithPartRoles() throws Exception {
        OrganizationMemberUpdateRequest organizationMemberUpdateRequest = new OrganizationMemberUpdateRequest();
        organizationMemberUpdateRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberUpdateRequest.setMemberId("sys_default_user");
        organizationMemberUpdateRequest.setUserRoleIds(List.of("sys_default_org_role_id_5", "sys_default_org_role_id_Y"));
        organizationMemberUpdateRequest.setProjectIds(List.of("sys_org_projectId1", "sys_org_projectId_Y"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER, organizationMemberUpdateRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserOne", "sys_default_organization_3", true, "sys_default_org_role_id_5", "sys_org_projectId1", true, "sys_default_org_role_id_Y", "sys_org_projectId_Y");
    }


    @Test
    @Order(6)
    public void updateOrgMemberError() throws Exception {
        //组织ID正确
        // 成员为空
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", null, Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().isBadRequest());
        //成员不存在
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", "sys_default_userX", Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().is5xxServerError());
        // 组织ID不存在
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_X", "sys_default_user2", Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().is5xxServerError());
        // 用户组为空
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", "sys_default_user2", Collections.emptyList(), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().isBadRequest());
        // 用户组都不存在
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", "sys_default_user2", Arrays.asList("sys_default_org_role_id_X", "sys_default_org_role_id_Y"), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().is5xxServerError());
        // 项目组都不存在
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", "sys_default_user2", Arrays.asList("sys_default_org_role_id_4", "sys_default_org_role_id_2"), Arrays.asList("sys_org_projectId_W", "sys_org_projectId1_Q"), status().is5xxServerError());
        // 成员不是当前组织的
        updateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "sys_default_organization_3", "sys_default_user4", Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), Arrays.asList("sys_org_projectId", "sys_org_projectId1"), status().is5xxServerError());
    }


    @Test
    @Order(7)
    public void updateOrgMemberToRoleSuccess() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user", "sys_default_user2"));
        organizationMemberRequest.setUserRoleIds(List.of("sys_default_org_role_id_4"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, organizationMemberRequest, status().isOk());
        organizationMemberRequest.setSelectAll(true);
        organizationMemberRequest.setExcludeIds(List.of("sys_default_user2"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserTwo", "sys_default_organization_3", true, "sys_default_org_role_id_4", null, false, null, null);
    }

    @Test
    @Order(8)
    public void updateOrgMemberToRoleError() throws Exception {
        //组织ID正确
        // 成员选择为空
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "sys_default_organization_3", Collections.emptyList(), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 成员都不存在
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "sys_default_organization_3", Arrays.asList("sys_default_userX", "sys_default_userY"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 用户组不存在
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "sys_default_organization_3", Arrays.asList("sys_default_user", "sys_default_user2"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "sys_default_organization_3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        // 组织不存在
        // 成员选择为空
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "default-organization-x", Collections.emptyList(), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        // 用户组不存在
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "default-organization-x", Arrays.asList("sys_default_user", "sys_default_user2"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "default-organization-x", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        //成员和用户组存在
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, "default-organization-x", Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().is5xxServerError());
        //组织为空
        addOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER_TO_ROLE, null, Arrays.asList("sys_default_user", "sys_default_user2"), Arrays.asList("sys_default_org_role_id_2", "sys_default_org_role_id_3"), status().isBadRequest());

    }


    @Test
    @Order(9)
    public void addOrgMemberToProjectSuccess() throws Exception {
        OrgMemberExtendProjectRequest organizationMemberRequest = new OrgMemberExtendProjectRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user", "sys_default_user2"));
        organizationMemberRequest.setProjectIds(Arrays.asList("sys_org_projectId2", "sys_org_projectId3"));
        this.requestPost(ORGANIZATION_PROJECT_ADD_MEMBER, organizationMemberRequest, status().isOk());
        organizationMemberRequest = new OrgMemberExtendProjectRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user"));
        organizationMemberRequest.setProjectIds(Arrays.asList("sys_org_projectId2"));
        this.requestPost(ORGANIZATION_PROJECT_ADD_MEMBER, organizationMemberRequest, status().isOk());
        organizationMemberRequest.setSelectAll(true);
        organizationMemberRequest.setExcludeIds(List.of("sys_org_projectId2"));
        this.requestPost(ORGANIZATION_PROJECT_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        listByKeyWord("testUserOne", "sys_default_organization_3", false, InternalUserRole.PROJECT_MEMBER.getValue(), "sys_org_projectId2", false, null, null);
    }

    @Test
    @Order(10)
    public void addOrgMemberToProjectError() throws Exception {
        // 成员选择为空
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "sys_default_organization_3", Collections.emptyList(), Arrays.asList("projectId1", "projectId2"), status().is5xxServerError());
        // 项目集合不存在
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "sys_default_organization_3", Arrays.asList("sys_default_user", "sys_default_user2"), Collections.emptyList(), status().isBadRequest());
        //成员和项目集合都为空
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "sys_default_organization_3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        // 成员选择为空
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "sys_default_organization_X", Collections.emptyList(), Arrays.asList("projectId1", "projectId2"), status().is5xxServerError());
    }

    @Test
    @Order(11)
    public void getOrgMemberListSuccess() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("sys_default_organization_3");
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user3"));
        organizationMemberRequest.setSelectAll(true);
        organizationMemberRequest.setExcludeIds(List.of("sys_default_userM"));
        organizationMemberRequest.setUserRoleIds(Arrays.asList("sys_default_org_role_id_3"));
        this.requestPost(ORGANIZATION_LIST_ADD_MEMBER, organizationMemberRequest, status().isOk());
        listByKeyWord(null, "sys_default_organization_3", false, null, null, false, null, null);
        organizationMemberRequest.setMemberIds(Arrays.asList("sys_default_user3"));
        organizationMemberRequest.setSelectAll(true);
        organizationMemberRequest.setUserRoleIds(Arrays.asList("sys_default_org_role_id_3"));
        this.requestPost(ORGANIZATION_LIST_ADD_MEMBER, organizationMemberRequest, status().isOk());
    }

    @Test
    @Order(12)
    public void getOrgMemberListSuccessWidthEmpty() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("testUserOne");
        organizationRequest.setOrganizationId("sys_default_organization_6");
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), organizationRequest.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= organizationRequest.getPageSize());
        //判断是否为空
        List<OrgUserExtend> orgUserExtends = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(orgUserExtends));

    }

    @Test
    @Order(13)
    public void getOrgMemberListError() throws Exception {
        // 页码有误
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(0);
        organizationRequest.setPageSize(10);
        organizationRequest.initKeyword("sys_default_user");
        organizationRequest.setOrganizationId("sys_default_organization_3");
        this.requestPost(ORGANIZATION_MEMBER_LIST, organizationRequest, status().isBadRequest());
        // 页数有误
        organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(1);
        organizationRequest.initKeyword("sys_default_user");
        organizationRequest.setOrganizationId("sys_default_organization_3");
        this.requestPost(ORGANIZATION_MEMBER_LIST, organizationRequest, status().isBadRequest());
    }


    @Test
    @Order(14)
    public void removeOrgMemberSuccess() throws Exception {
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/sys_default_organization_6/sys_default_user4", status().isOk());
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/sys_default_organization_6/sys_default_user", status().is5xxServerError());
    }

    @Test
    @Order(15)
    public void removeOrgMemberSuccessWithNoProject() throws Exception {
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/sys_default_organization_3/sys_default_user4", status().isOk());
    }

    @Test
    @Order(16)
    public void removeOrgMemberError() throws Exception {
        // 项目不存在
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/default-organization-x/admin-x", status().is5xxServerError());
    }

    @Test
    @Order(17)
    public void getProjectListByOrgSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/project/list/sys_default_organization_3")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectList));
        List<String> ids = projectList.stream().map(OptionDTO::getId).toList();
        Assertions.assertTrue(ids.contains("sys_org_projectId"));
        List<String> names = projectList.stream().map(OptionDTO::getName).toList();
        Assertions.assertTrue(names.contains("sys_org_projectId"));
    }

    @Test
    @Order(18)
    public void getProjectEmptyListByOrg() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/project/list/sys_default_organization_5")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isEmpty(projectList));

    }

    @Test
    @Order(19)
    public void getProjectListByOrgError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organization/project/list/sys_default_organization_x")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }


    @Test
    @Order(20)
    public void getUserRoleListByOrgSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/user/role/list/sys_default_organization_3")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectList));
        List<String> ids = projectList.stream().map(OptionDTO::getId).toList();
        Assertions.assertTrue(ids.contains("sys_default_org_role_id_3"));
        List<String> names = projectList.stream().map(OptionDTO::getName).toList();
        Assertions.assertTrue(names.contains("sys_default_org_role_id_3"));
    }

    @Test
    @Order(21)
    public void getUserRoleEmptyListByOrg() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/user/role/list/sys_default_organization_6")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> userRoleList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(userRoleList));
        List<String> ids = userRoleList.stream().map(OptionDTO::getId).toList();
        Assertions.assertTrue(ids.contains("org_admin"));
        Assertions.assertTrue(ids.contains("org_member"));
        List<String> names = userRoleList.stream().map(OptionDTO::getName).toList();
        Assertions.assertTrue(names.contains("组织管理员"));
        Assertions.assertTrue(names.contains("组织成员"));

    }

    @Test
    @Order(22)
    public void getUserRoleListByOrgError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organization/user/role/list/sys_default_organization_x")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }


    @Test
    @Order(23)
    public void getNotExistUserListByOrgSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/not-exist/user/list/sys_default_organization_3")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("keyword", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectList));
        List<String> ids = projectList.stream().map(OptionDTO::getId).toList();
        Assertions.assertTrue(ids.contains("sys_default_user5"));
        List<String> names = projectList.stream().map(OptionDTO::getName).toList();
        Assertions.assertTrue(names.contains("testUserFive"));
    }

    @Test
    @Order(24)
    public void getNotExistUserListWithNoRelation() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/organization/not-exist/user/list/sys_default_organization_5")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> projectList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(projectList));

    }

    @Test
    @Order(25)
    public void getNotExistUserListByOrgError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/organization/not-exist/user/list/sys_default_organization_x")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }


    private void listByKeyWord(String keyWord, String orgId, boolean compare, String userRoleId, String projectId, boolean checkPart, String noUserRoleId, String noProjectId) throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.initKeyword(keyWord);
        organizationRequest.setOrganizationId(orgId);
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), organizationRequest.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= organizationRequest.getPageSize());
        // 返回值中取出第一条数据, 并判断是否包含关键字admin
        OrgUserExtend orgUserExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class).getFirst();
        if (StringUtils.isNotBlank(keyWord)) {
            Assertions.assertTrue(StringUtils.contains(orgUserExtend.getName(), organizationRequest.getKeyword())
                    || StringUtils.contains(orgUserExtend.getEmail(), organizationRequest.getKeyword())
                    || StringUtils.contains(orgUserExtend.getPhone(), organizationRequest.getKeyword()));
        }
        if (compare) {
            Assertions.assertNotNull(orgUserExtend.getUserRoleIdNameMap());
            List<String> userRoleIds = orgUserExtend.getUserRoleIdNameMap().stream().map(OptionDTO::getId).toList();
            Assertions.assertTrue(userRoleIds.contains(userRoleId));

            if (StringUtils.isNotBlank(projectId)) {
                Assertions.assertNotNull(orgUserExtend.getProjectIdNameMap());
                List<String> projectIds = orgUserExtend.getProjectIdNameMap().stream().map(OptionDTO::getId).toList();
                Assertions.assertTrue(projectIds.contains(projectId));
                List<String> projectNames = orgUserExtend.getProjectIdNameMap().stream().map(OptionDTO::getName).toList();
                Assertions.assertTrue(projectNames.contains(projectId));
            }
        }
        if (checkPart) {
            Assertions.assertNotNull(orgUserExtend.getUserRoleIdNameMap());
            List<String> userRoleIds = orgUserExtend.getUserRoleIdNameMap().stream().map(OptionDTO::getId).toList();
            Assertions.assertFalse(userRoleIds.contains(noUserRoleId));

            if (StringUtils.isNotBlank(noProjectId)) {
                Assertions.assertNotNull(orgUserExtend.getProjectIdNameMap());
                List<String> projectIds = orgUserExtend.getProjectIdNameMap().stream().map(OptionDTO::getId).toList();
                Assertions.assertFalse(projectIds.contains(noProjectId));
            }

        }
    }

    private void addOrganizationMemberError(String url, String organizationId, List<String> userIds, List<String> userGroupIds, ResultMatcher resultMatcher) throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId(organizationId);
        organizationMemberRequest.setMemberIds(userIds);
        organizationMemberRequest.setUserRoleIds(userGroupIds);
        this.requestPost(url, organizationMemberRequest, resultMatcher);
    }

    private void updateOrganizationMemberError(String url, String organizationId, String memberId, List<String> userRoleIds, List<String> projectIds, ResultMatcher resultMatcher) throws Exception {
        OrganizationMemberUpdateRequest organizationMemberUpdateRequest = new OrganizationMemberUpdateRequest();
        organizationMemberUpdateRequest.setOrganizationId(organizationId);
        organizationMemberUpdateRequest.setMemberId(memberId);
        organizationMemberUpdateRequest.setUserRoleIds(userRoleIds);
        organizationMemberUpdateRequest.setProjectIds(projectIds);
        this.requestPost(url, organizationMemberUpdateRequest, resultMatcher);
    }


    private void addOrUpdateOrganizationProjectMemberError(String url, String organizationId, List<String> userIds, List<String> projectIds, ResultMatcher resultMatcher) throws Exception {
        OrgMemberExtendProjectRequest organizationMemberRequest = new OrgMemberExtendProjectRequest();
        organizationMemberRequest.setOrganizationId(organizationId);
        organizationMemberRequest.setMemberIds(userIds);
        organizationMemberRequest.setProjectIds(projectIds);
        this.requestPost(url, organizationMemberRequest, resultMatcher);
    }


    private void requestGet(String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
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

    private MvcResult responsePost(OrganizationRequest organizationRequest) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(OrganizationControllerTests.ORGANIZATION_MEMBER_LIST)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(organizationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }
}
