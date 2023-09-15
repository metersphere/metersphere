package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.system.ldap.service.LdapService;
import io.metersphere.system.ldap.vo.LdapLoginRequest;
import io.metersphere.system.ldap.vo.LdapRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.request.AuthSourceRequest;
import io.metersphere.system.request.AuthSourceStatusRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthSourceControllerTests extends BaseTest {

    public static final String AUTH_SOURCE_ADD = "/system/authsource/add";

    public static final String AUTH_SOURCE_LIST = "/system/authsource/list";

    public static final String AUTH_SOURCE_UPDATE = "/system/authsource/update";

    public static final String AUTH_SOURCE_GET = "/system/authsource/get/";

    public static final String AUTH_SOURCE_DELETE = "/system/authsource/delete/";

    private static final ResultMatcher CLIENT_ERROR_MATCHER = status().is4xxClientError();

    public static final String AUTH_SOURCE_UPDATE_STATUS = "/system/authsource/update/status";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    public static final String LDAP_TEST_CONNECT = "/system/authsource/ldap/test-connect";

    public static final String LDAP_TEST_LOGIN = "/system/authsource/ldap/test-login";

    @Mock
    private LdapService ldapService;
    @Resource
    AuthSourceController authSourceController;

    @Test
    @Order(1)
    public void testAddSource() throws Exception {
        AuthSourceRequest authSource = new AuthSourceRequest();
        authSource.setName("测试CAS");
        authSource.setType("CAS");
        this.requestPost(AUTH_SOURCE_ADD, authSource, ERROR_REQUEST_MATCHER);
        authSource.setConfiguration("123");
        this.requestPost(AUTH_SOURCE_ADD, authSource);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_ADD, AUTH_SOURCE_ADD, authSource);
    }

    @Test
    @Order(2)
    public void testGetSourceList() throws Exception {
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(10);
        this.requestPost(AUTH_SOURCE_LIST, basePageRequest);
        basePageRequest.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        this.requestPost(AUTH_SOURCE_LIST, basePageRequest);

        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ, AUTH_SOURCE_LIST, basePageRequest);
    }


    @Test
    @Order(3)
    public void testUpdateSource() throws Exception {
        List<AuthSourceRequest> authSourceList = this.getAuthSourceList();
        AuthSourceRequest authSource = new AuthSourceRequest();
        authSource.setId("authsource_id");
        authSource.setConfiguration("123666");
        authSource.setName("更新");
        authSource.setType("CAS");
        this.requestPost(AUTH_SOURCE_UPDATE, authSource);
        authSource.setId(authSourceList.get(0).getId());

        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE, AUTH_SOURCE_UPDATE, authSource);
    }

    @Test
    @Order(4)
    public void testUpdateStatus() throws Exception {

        List<AuthSourceRequest> authSourceList = this.getAuthSourceList();
        AuthSourceStatusRequest request = new AuthSourceStatusRequest();
        request.setId(authSourceList.get(0).getId());
        this.requestPost(AUTH_SOURCE_UPDATE_STATUS, request);
        request.setEnable(false);
        this.requestPost(AUTH_SOURCE_UPDATE_STATUS, request);
        requestPostPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_UPDATE, AUTH_SOURCE_UPDATE_STATUS, request);
    }


    @Test
    @Order(5)
    public void testGetSourceById() throws Exception {
        List<AuthSourceRequest> authSourceList = this.getAuthSourceList();
        String url = AUTH_SOURCE_GET + authSourceList.get(0).getId();
        this.requestGet(url);

        requestGetPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ, url);
    }


    @Test
    @Order(6)
    public void testDelSourceById() throws Exception {
        List<AuthSourceRequest> authSourceList = this.getAuthSourceList();
        String url = AUTH_SOURCE_DELETE + authSourceList.get(0).getId();
        this.requestGet(url);

        requestGetPermissionTest(PermissionConstants.SYSTEM_PARAMETER_SETTING_AUTH_READ_DELETE, url);
    }


    @Test
    @Order(7)
    public void testAddSourceError() throws Exception {
        AuthSource authSource = new AuthSource();
        authSource.setConfiguration("123".getBytes());
        this.requestPost(AUTH_SOURCE_ADD, authSource, CLIENT_ERROR_MATCHER);
    }


    private MvcResult requestPost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                
                .andReturn();
    }

    private MvcResult requestGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk()).andReturn();
    }

    private List<AuthSourceRequest> getAuthSourceList() throws Exception {
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(10);
        MvcResult mvcResult = this.requestPost(AUTH_SOURCE_LIST, basePageRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<?> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        List<AuthSourceRequest> authSourceRequests = JSON.parseArray(JSON.toJSONString(returnPager.getList()), AuthSourceRequest.class);
        return authSourceRequests;
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


    @Test
    @Order(11)
    public void testLdapConnectMock() throws Exception {
        authSourceController.setLdapService(ldapService);
        LdapRequest ldapRequest = getRequest("ldaps://127.1.1.1", "cn=admin,dc=example,dc=org", "admin");
        Mockito.doNothing().when(ldapService).testConnect(ldapRequest);
        this.requestPostAndReturn(LDAP_TEST_CONNECT, ldapRequest);
    }

    @Test
    @Order(12)
    public void testLdapLoginMock() throws Exception {
        authSourceController.setLdapService(ldapService);
        LdapLoginRequest loginRequest = getLoginRequest("ldap://127.1.1.1", "cn=admin,dc=example,dc=org", "admin", "cn=admin,dc=example,dc=org", "(|(uid={0})(mail={0}))", "{\"username\":\"uid\",\"name\":\"cn\",\"email\":\"mail\"}", "admin", "admin");
        DirContextOperations operations = new DirContextAdapter();
        Mockito.when(ldapService.testLogin(loginRequest)).thenReturn(operations);
        this.requestPostAndReturn(LDAP_TEST_LOGIN, loginRequest);
    }

    private LdapLoginRequest getLoginRequest(String ldapUrl, String ldapDn, String ldapPassword, String ldapUserOu, String ldapUserFilter, String ldapUserMapping, String username, String password) {
        LdapLoginRequest loginRequest = new LdapLoginRequest();
        loginRequest.setLdapUrl(ldapUrl);
        loginRequest.setLdapDn(ldapDn);
        loginRequest.setLdapPassword(ldapPassword);
        loginRequest.setLdapUserOu(ldapUserOu);
        loginRequest.setLdapUserFilter(ldapUserFilter);
        loginRequest.setLdapUserMapping(ldapUserMapping);
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        return loginRequest;
    }


    private LdapRequest getRequest(String ldapUrl, String ldapDn, String ldapPassword) {
        LdapRequest ldapRequest = new LdapRequest();
        ldapRequest.setLdapUrl(ldapUrl);
        ldapRequest.setLdapDn(ldapDn);
        ldapRequest.setLdapPassword(ldapPassword);
        return ldapRequest;
    }
}
