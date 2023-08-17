package io.metersphere.system.controller;

import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import io.metersphere.system.request.ProjectRequest;
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
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemOrganizationControllerTests extends BaseTest{

    @Resource
    private MockMvc mockMvc;

    public static final String ORGANIZATION_LIST = "/system/organization/list";
    public static final String ORGANIZATION_LIST_OPTION_ALL = "/system/organization/option/all";
    public static final String ORGANIZATION_DEFAULT = "/system/organization/default";
    public static final String ORGANIZATION_LIST_MEMBER = "/system/organization/list-member";
    public static final String ORGANIZATION_ADD_MEMBER = "/system/organization/add-member";
    public static final String ORGANIZATION_REMOVE_MEMBER = "/system/organization/remove-member";
    public static final String ORGANIZATION_LIST_PROJECT = "/system/organization/list-project";
    public static final String ORGANIZATION_MEMBER_OPTION = "/system/user/get-option";
    public static final String ORGANIZATION_TOTAL = "/system/organization/total";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_organization.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testListOrganizationSuccess() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("default");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST, organizationRequest);
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
        // 返回值中取出第一条数据, 并判断是否包含关键字default
        OrganizationDTO organizationDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), OrganizationDTO.class).get(0);
        Assertions.assertTrue(StringUtils.contains(organizationDTO.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(organizationDTO.getId(), organizationRequest.getKeyword()));

        // sort不为空
        Map<String, String> sort = new HashMap<>();
        sort.put("id", "desc");
        organizationRequest.setSort(sort);
        MvcResult sortResult = this.responsePost(ORGANIZATION_LIST, organizationRequest);
        String sortData = sortResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-organization-6
        OrganizationDTO organizationDTO1 = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), OrganizationDTO.class).get(0);
        Assertions.assertTrue(organizationDTO1.getId().contains("default"));
        // 权限校验
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, ORGANIZATION_LIST, organizationRequest);
    }

    @Test
    @Order(1)
    public void testListOrganizationEmptySuccess() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("default-x");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST, organizationRequest);
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
        // 返回的数据量为0条
        Assertions.assertEquals(0, pageData.getTotal());
        // 权限校验
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, ORGANIZATION_LIST, organizationRequest);
    }

    @Test
    @Order(2)
    public void testListOrganizationError() throws Exception {
        // 页码有误
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(0);
        organizationRequest.setPageSize(10);
        this.requestPost(ORGANIZATION_LIST, organizationRequest, status().isBadRequest());
        // 页数有误
        organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(1);
        this.requestPost(ORGANIZATION_LIST, organizationRequest, status().isBadRequest());
    }

    @Test
    @Order(3)
    public void testListAllOrganizationSuccess() throws Exception {
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST_OPTION_ALL, null);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回值不为空
        Assertions.assertNotNull(resultHolder);
        // 返回总条数是否大于0
        Assertions.assertFalse(JSON.parseArray(JSON.toJSONString(resultHolder.getData())).isEmpty());
    }

    @Test
    @Order(4)
    public void testListAllOrganizationError() throws Exception {
        this.requestGet(ORGANIZATION_LIST_OPTION_ALL, status().isMethodNotAllowed());
    }

    @Test
    @Order(5)
    public void testListOrganizationMemberSuccess() throws Exception {
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST_MEMBER, organizationRequest);
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
        UserExtend userExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), UserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(userExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getPhone(), organizationRequest.getKeyword()));

        // sort不为空
        Map<String, String> sort = new HashMap<>();
        sort.put("id", "desc");
        organizationRequest.setSort(sort);
        MvcResult sortResult = this.responsePost(ORGANIZATION_LIST_MEMBER, organizationRequest);
        String sortData = sortResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-admin
        UserExtend userExtend1 = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), UserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(userExtend1.getId(), "default-admin"));
    }

    @Test
    @Order(6)
    public void testListOrganizationMemberError() throws Exception {
        // 页码有误
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(0);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_LIST_MEMBER, organizationRequest, status().isBadRequest());
        // 页数有误
        organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(1);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_LIST_MEMBER, organizationRequest, status().isBadRequest());
    }


    @Test
    @Order(7)
    public void testAddOrganizationMemberSuccess() throws Exception {
        OrganizationMemberRequest organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(List.of("admin", "default-admin"));
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 日志校验
        checkLog(organizationMemberRequest.getOrganizationId(), OperationLogType.ADD);
        // 批量添加成员成功后, 验证是否添加成功
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-3");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST_MEMBER, organizationRequest);
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
        UserExtend userExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), UserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(userExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getPhone(), organizationRequest.getKeyword()));
        // 权限校验
        organizationMemberRequest.setMemberIds(List.of("admin"));
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD, ORGANIZATION_ADD_MEMBER, organizationMemberRequest);
    }

    @Test
    @Order(8)
    public void testAddOrganizationMemberSuccessWithRepeatUser() throws Exception {
        OrganizationMemberRequest organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(List.of("admin"));
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().isOk());
        // 批量添加成员成功后, 验证是否添加成功
        OrganizationRequest organizationRequest = new OrganizationRequest();
        organizationRequest.setCurrent(1);
        organizationRequest.setPageSize(10);
        organizationRequest.setKeyword("admin");
        organizationRequest.setOrganizationId("default-organization-3");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST_MEMBER, organizationRequest);
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
        UserExtend userExtend = JSON.parseArray(JSON.toJSONString(pageData.getList()), UserExtend.class).get(0);
        Assertions.assertTrue(StringUtils.contains(userExtend.getName(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getEmail(), organizationRequest.getKeyword())
                || StringUtils.contains(userExtend.getPhone(), organizationRequest.getKeyword()));
    }

    @Test
    @Order(9)
    public void testAddOrganizationMemberError() throws Exception {
        // 成员选择为空
        OrganizationMemberRequest organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Collections.emptyList());
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().isBadRequest());
        // 成员都不存在
        organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("SccNotExistOne", "SccNotExistTwo"));
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().is5xxServerError());
        // 成员有一个不存在
        organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-3");
        organizationMemberRequest.setMemberIds(Arrays.asList("SccNotExistOne", "default-admin"));
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().is5xxServerError());
        // 组织不存在
        organizationMemberRequest = new OrganizationMemberRequest();
        organizationMemberRequest.setOrganizationId("default-organization-x");
        organizationMemberRequest.setMemberIds(Arrays.asList("admin", "default-admin"));
        this.requestPost(ORGANIZATION_ADD_MEMBER, organizationMemberRequest, status().is5xxServerError());
    }

    @Test
    @Order(10)
    public void testRemoveOrganizationMemberSuccess() throws Exception {
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/default-organization-3/admin", status().isOk());
        // 日志校验
        checkLog("default-organization-3", OperationLogType.DELETE);
        // 权限校验
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE, ORGANIZATION_REMOVE_MEMBER + "/default-organization-3/admin");
    }

    @Test
    @Order(11)
    public void testRemoveOrganizationMemberError() throws Exception {
        // 组织不存在
        this.requestGet(ORGANIZATION_REMOVE_MEMBER + "/default-organization-x/admin-x", status().is5xxServerError());
    }

    @Test
    @Order(12)
    public void testGetOrganizationProjectSuccess() throws Exception {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(10);
        projectRequest.setOrganizationId("default-organization-2");
        MvcResult mvcResult = this.responsePost(ORGANIZATION_LIST_PROJECT, projectRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), projectRequest.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= projectRequest.getPageSize());

        // sort不为空
        Map<String, String> sort = new HashMap<>();
        sort.put("id", "desc");
        projectRequest.setSort(sort);
        MvcResult sortResult = this.responsePost(ORGANIZATION_LIST_PROJECT, projectRequest);
        String sortData = sortResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-project
        ProjectDTO projectDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectDTO.class).get(0);
        Assertions.assertTrue(StringUtils.equals(projectDTO.getId(), "default-project"));

        // 权限校验
        requestPostPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, ORGANIZATION_LIST_PROJECT, projectRequest);
    }

    @Test
    @Order(13)
    public void testGetOrganizationProjectError() throws Exception {
        // 页码有误
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setCurrent(0);
        projectRequest.setPageSize(10);
        projectRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_LIST_PROJECT, projectRequest, status().isBadRequest());
        // 页数有误
        projectRequest = new ProjectRequest();
        projectRequest.setCurrent(1);
        projectRequest.setPageSize(1);
        projectRequest.setOrganizationId("default-organization-2");
        this.requestPost(ORGANIZATION_LIST_PROJECT, projectRequest, status().isBadRequest());
    }

    @Test
    @Order(14)
    public void testGetDefaultOrganizationSuccess() throws Exception {
        MvcResult mvcResult = this.responseGet(SystemOrganizationControllerTests.ORGANIZATION_DEFAULT);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        OrganizationDTO defaultOrg = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), OrganizationDTO.class);
        // 返回值不为空
        Assertions.assertNotNull(defaultOrg);
        // 返回数据NUM是否为默认100001
        Assertions.assertEquals(defaultOrg.getNum(), 100001L);

        // 权限校验
        requestGetPermissionTest(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, ORGANIZATION_DEFAULT);
    }

    @Test
    @Order(15)
    public void testGetDefaultOrganizationError() throws Exception {
        this.requestPost(ORGANIZATION_DEFAULT, null, status().isMethodNotAllowed());
    }

    @Test
    @Order(16)
    public void testGetOrganizationMemberOption() throws Exception {
        MvcResult mvcResult = this.responseGet(SystemOrganizationControllerTests.ORGANIZATION_MEMBER_OPTION + "/default-organization-2");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        // 权限校验
        requestGetPermissionTest(PermissionConstants.SYSTEM_USER_READ, SystemOrganizationControllerTests.ORGANIZATION_MEMBER_OPTION + "/default-organization-2");
    }

    @Test
    @Order(17)
    public void testGetTotal() throws Exception {
        // 组织不存在
        MvcResult mvcResult = this.responseGet(SystemOrganizationControllerTests.ORGANIZATION_TOTAL + "?organizationId=default-organization-2");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Map<String, Integer> resultHolder = JSON.parseObject(returnData, Map.class);

        mvcResult = this.responseGet(SystemOrganizationControllerTests.ORGANIZATION_TOTAL);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, Map.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
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
