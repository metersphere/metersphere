package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.request.*;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationControllerTests extends BaseTest{

    @Resource
    private MockMvc mockMvc;


    public static final String ORGANIZATION_LIST_ADD_MEMBER = "/organization/add-member";
    public static final String ORGANIZATION_UPDATE_MEMBER = "/organization/update-member";
    public static final String ORGANIZATION_REMOVE_MEMBER = "/organization/remove-member";
    public static final String ORGANIZATION_MEMBER_LIST = "/organization/member/list";
    public static final String ORGANIZATION_PROJECT_ADD_MEMBER = "/organization/project/add-member";
    
    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_organization.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testListAddOrganizationMemberSuccess() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("admin", "default-admin"));
        organizationMemberRequest.setUserGroupIds(Arrays.asList("default-org-role-id-2", "default-org-role-id-3"));
        this.requestPost(ORGANIZATION_LIST_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-3");
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
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
        OrgUserExtend orgUserExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(orgUserExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getPhone(), organizationRequest.getKeyword()));
    }

    @Test
    @Order(1)
    public void testListAddOrganizationMemberError() throws Exception {
        //组织ID正确
        // 成员选择为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-3", Collections.emptyList(), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());
        // 用户组不存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-3", Arrays.asList("admin", "default-admin"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        // 组织不存在
        // 成员选择为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Collections.emptyList(), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());
        // 用户组不存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Arrays.asList("admin", "default-admin"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        //成员和用户组存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, "default-organization-x", Arrays.asList("admin", "default-admin"), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().is5xxServerError());
        //组织为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_LIST_ADD_MEMBER, null, Arrays.asList("admin", "default-admin"), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());

    }

    @Test
    @Order(2)
    public void testUpdateOrganizationMemberSuccess() throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("admin", "default-admin"));
        organizationMemberRequest.setUserGroupIds(List.of("default-org-role-id-1"));
        this.requestPost(ORGANIZATION_UPDATE_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-3");
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
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
        OrgUserExtend orgUserExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(orgUserExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getPhone(), organizationRequest.getKeyword()));
    }

    @Test
    @Order(3)
    public void testUpdateOrganizationMemberError() throws Exception {
        //组织ID正确
        // 成员选择为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-3", Collections.emptyList(), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());
        // 用户组不存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-3", Arrays.asList("admin", "default-admin"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        // 组织不存在
        // 成员选择为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-x", Collections.emptyList(), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());
        // 用户组不存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-x", Arrays.asList("admin", "default-admin"), Collections.emptyList(), status().isBadRequest());
        //成员和用户组都为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-x", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
        //成员和用户组存在
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, "default-organization-x", Arrays.asList("admin", "default-admin"), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().is5xxServerError());
        //组织为空
        addOrUpdateOrganizationMemberError(ORGANIZATION_UPDATE_MEMBER, null, Arrays.asList("admin", "default-admin"), Arrays.asList("default-org-role-id-2", "default-org-role-id-3"), status().isBadRequest());

    }

    @Test
    @Order(4)
    public void testOrganizationMemberListSuccess() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
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
        OrgUserExtend orgUserExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(orgUserExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getPhone(), organizationRequest.getKeyword()));
    }

    @Test
    @Order(5)
    public void testOrganizationMemberListError() throws Exception {
        // 页码有误
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(0);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_MEMBER_LIST, organizationRequest, status().isBadRequest());
        // 页数有误
        organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(1);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_MEMBER_LIST, organizationRequest, status().isBadRequest());
    }


    @Test
    @Order(6)
    @Sql(scripts = {"/dml/init_project.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testAddOrgProjectMemberSuccess() throws Exception {
        OrgMemberExtendProjectRequest organizationMemberRequest = new OrgMemberExtendProjectRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("admin", "default-admin"));
        organizationMemberRequest.setProjectIds(Arrays.asList("projectId1", "projectId2"));
        this.requestPost(ORGANIZATION_PROJECT_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-3");
        MvcResult mvcResult = this.responsePost(organizationRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
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
        OrgUserExtend orgUserExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrgUserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(orgUserExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(orgUserExtend.getPhone(), organizationRequest.getKeyword()));
    }

    @Test
    @Order(7)
    public void testAddOrgProjectMemberError() throws Exception {
        // 成员选择为空
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "default-organization-3", Collections.emptyList(), Arrays.asList("projectId1", "projectId2"), status().isBadRequest());
        // 项目集合不存在
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "default-organization-3", Arrays.asList("admin", "default-admin"), Collections.emptyList(), status().isBadRequest());
        //成员和项目集合都为空
        addOrUpdateOrganizationProjectMemberError(ORGANIZATION_PROJECT_ADD_MEMBER, "default-organization-3", Collections.emptyList(), Collections.emptyList(), status().isBadRequest());
    }

    @Test
    @Order(8)
    public void testRemoveOrganizationMemberSuccess() throws Exception {
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/default-organization-3/admin", status().isOk());
    }

    @Test
    @Order(9)
    public void testRemoveOrganizationMemberError() throws Exception {
        // 项目不存在
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/default-organization-x/admin-x", status().is5xxServerError());
    }

    private void addOrUpdateOrganizationMemberError(String url, String organizationId, List<String> memberIds, List<String> userGroupIds, ResultMatcher resultMatcher) throws Exception {
        OrganizationMemberExtendRequest organizationMemberRequest = new OrganizationMemberExtendRequest();
        organizationMemberRequest.setOrganizationId(organizationId);
        organizationMemberRequest.setMemberIds(memberIds);
        organizationMemberRequest.setUserGroupIds(userGroupIds);
        this.requestPost(url, organizationMemberRequest, resultMatcher);
    }

    private void addOrUpdateOrganizationProjectMemberError(String url, String organizationId, List<String> memberIds, List<String> projectIds, ResultMatcher resultMatcher) throws Exception {
        OrgMemberExtendProjectRequest organizationMemberRequest = new OrgMemberExtendProjectRequest();
        organizationMemberRequest.setOrganizationId(organizationId);
        organizationMemberRequest.setMemberIds(memberIds);
        organizationMemberRequest.setProjectIds(projectIds);
        this.requestPost(url, organizationMemberRequest, resultMatcher);
    }


    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .header(SessionConstants.HEADER_TOKEN, sessionId)
                    .header(SessionConstants.CSRF_TOKEN, csrfToken)
                    .content(JSON.toJSONString(param))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(resultMatcher).andDo(print())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private MvcResult responsePost(OrganizationRequest organizationRequest) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(OrganizationControllerTests.ORGANIZATION_MEMBER_LIST)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(organizationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }
}
