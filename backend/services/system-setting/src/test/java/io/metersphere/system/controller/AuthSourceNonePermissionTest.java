package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.mapper.UserRolePermissionMapper;
import io.metersphere.system.request.AuthSourceRequest;
import jakarta.annotation.Resource;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class AuthSourceNonePermissionTest {

    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;

    private static final String NONE_ROLE_USERNAME = "permission@fit2cloud.com";

    private static final String NONE_ROLE_PASSWORD = "permission@fit2cloud.com";

    private static final String ROLE_ID = "org_admin";

    public static final String AUTH_SOURCE_ADD = "/system/authsource/add";

    public static final String AUTH_SOURCE_List = "/system/authsource/list";

    public static final String AUTH_SOURCE_UPDATE = "/system/authsource/update";

    public static final String AUTH_SOURCE_GET = "/system/authsource/get/";

    public static final String AUTH_SOURCE_DELETE = "/system/authsource/delete/";

    private static final ResultMatcher CHECK_RESULT_MATHER = status().isForbidden();

    private static final ResultMatcher CHECK_RESULT_OK = status().isOk();


    public static final String BASE_INFO_SAVE_URL = "/system/parameter/save/base-info";

    public static final String BASE_INFO_URL = "/system/parameter/get/base-info";

    public static final String EMAIL_INFO_URL = "/system/parameter/get/email-info";

    public static final String EMAIL_INFO_SAVE_URL = "/system/parameter/edit/email-info";

    public static final String EMAIL_INFO_TEST_CONNECT_URL = "/system/parameter/test/email";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;

    @BeforeEach
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content("{\"username\":\"" + NONE_ROLE_USERNAME + "\",\"password\":\"" + NONE_ROLE_PASSWORD + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
    }

    public AuthSourceRequest getAuthSource() {
        AuthSourceRequest authSource = new AuthSourceRequest();
        authSource.setId(UUID.randomUUID().toString());
        authSource.setConfiguration("123");
        authSource.setName("测试CAS_" + UUID.randomUUID().toString());
        authSource.setType("CAS");
        return authSource;
    }


    /**
     * 无权限
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_permission_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testNoPermission() throws Exception {

        //认证配置
        AuthSourceRequest authSource = this.getAuthSource();
        //校验权限: 添加认证权限
        this.requestPost(AUTH_SOURCE_ADD, authSource, CHECK_RESULT_MATHER);

        //权限校验: 查询认证权限
        this.testGetSourceList(CHECK_RESULT_MATHER);

        //权限校验: 修改认证权限
        authSource.setName("测试CAS修改");
        this.testUpdateSource(authSource, CHECK_RESULT_MATHER);

        //权限校验: 修改认证权限状态
        this.testUpdateStatus(authSource, CHECK_RESULT_MATHER);

        //权限校验: 查询认证权限详情
        this.testGetSourceById(authSource, CHECK_RESULT_MATHER);

        //权限校验: 删除认证权限
        this.testDelSourceById(authSource, CHECK_RESULT_MATHER);


        //基本配置
        List<SystemParameter> systemParameters = getSystemParameters();
        //403
        //权限校验: 保存+编辑基础信息
        this.testSaveBaseInfo(systemParameters, CHECK_RESULT_MATHER);
        //权限校验: 获取用户信息
        this.testGetBaseInfo(CHECK_RESULT_MATHER);
        //权限校验: 保存+编辑邮件设置
        this.testEditEmailInfo(CHECK_RESULT_MATHER);
        //权限校验: 获取邮件设置
        this.testGetEmailInfo(CHECK_RESULT_MATHER);
        //权限校验: 测试邮件连接
        this.testEmailConnect(CHECK_RESULT_MATHER);

    }

    /**
     * 只读权限
     *
     * @throws Exception
     */
    @Test
    @Order(2)
    public void testReadPermission() throws Exception {
        //添加读权限
        addPermission(PermissionConstants.SYSTEM_SETTING_READ);


        //获取最新权限
        this.requestGet("/is-login", CHECK_RESULT_OK);
        //认证配置
        AuthSourceRequest authSource = this.getAuthSource();
        //403
        this.testAddSource(authSource, CHECK_RESULT_MATHER);
        //200
        this.testGetSourceList(CHECK_RESULT_OK);
        this.testGetSourceById(authSource, CHECK_RESULT_OK);
        //403
        authSource.setName("测试CAS修改");
        this.testUpdateSource(authSource, CHECK_RESULT_MATHER);
        this.testUpdateStatus(authSource, CHECK_RESULT_MATHER);
        this.testDelSourceById(authSource, CHECK_RESULT_MATHER);


        //基本配置
        List<SystemParameter> systemParameters = getSystemParameters();
        //403
        this.testSaveBaseInfo(systemParameters, CHECK_RESULT_MATHER);
        //200
        this.testGetBaseInfo(CHECK_RESULT_OK);
        //403
        this.testEditEmailInfo(CHECK_RESULT_MATHER);
        //200
        this.testGetEmailInfo(CHECK_RESULT_OK);
        //有权限  连接不通返回500
        this.testEmailConnect(ERROR_REQUEST_MATCHER);
    }


    @Test
    @Order(3)
    public void testAddPermission() throws Exception {

        addPermission(PermissionConstants.SYSTEM_SETTING_READ_CREAT);
        //获取最新权限
        this.requestGet("/is-login", CHECK_RESULT_OK);

        AuthSourceRequest authSource = this.getAuthSource();
        //200
        this.testAddSource(authSource, CHECK_RESULT_OK);
        this.testGetSourceList(CHECK_RESULT_OK);
        this.testGetSourceById(authSource, CHECK_RESULT_OK);

        //403
        authSource.setName("测试CAS修改");
        this.testUpdateSource(authSource, CHECK_RESULT_MATHER);
        this.testUpdateStatus(authSource, CHECK_RESULT_MATHER);
        this.testDelSourceById(authSource, CHECK_RESULT_MATHER);



        //基本配置
        List<SystemParameter> systemParameters = getSystemParameters();
        //200
        this.testSaveBaseInfo(systemParameters, CHECK_RESULT_OK);
        this.testGetBaseInfo(CHECK_RESULT_OK);
        this.testEditEmailInfo(CHECK_RESULT_OK);
        this.testGetEmailInfo(CHECK_RESULT_OK);
        this.testEmailConnect(ERROR_REQUEST_MATCHER);

    }


    @Test
    @Order(4)
    public void testUpdatePermission() throws Exception {

        addPermission(PermissionConstants.SYSTEM_SETTING_READ_UPDATE);
        //获取最新权限
        this.requestGet("/is-login", CHECK_RESULT_OK);

        //认证配置
        AuthSourceRequest authSource = this.getAuthSource();
        //200
        this.testAddSource(authSource, CHECK_RESULT_OK);
        this.testGetSourceList(CHECK_RESULT_OK);
        this.testGetSourceById(authSource, CHECK_RESULT_OK);
        authSource.setName("测试CAS修改");
        this.testUpdateSource(authSource, CHECK_RESULT_OK);
        this.testUpdateStatus(authSource, CHECK_RESULT_OK);
        //403
        this.testDelSourceById(authSource, CHECK_RESULT_MATHER);

    }

    @Test
    @Order(5)
    public void testDeletePermission() throws Exception {
        addPermission(PermissionConstants.SYSTEM_SETTING_READ_DELETE);
        //获取最新权限
        this.requestGet("/is-login", CHECK_RESULT_OK);

        AuthSourceRequest authSource = this.getAuthSource();
        //200
        this.testAddSource(authSource, CHECK_RESULT_OK);
        this.testGetSourceList(CHECK_RESULT_OK);
        this.testGetSourceById(authSource, CHECK_RESULT_OK);
        authSource.setName("测试CAS修改");
        this.testUpdateSource(authSource, CHECK_RESULT_OK);
        this.testUpdateStatus(authSource, CHECK_RESULT_OK);
        this.testDelSourceById(authSource, CHECK_RESULT_OK);

    }


    private void addPermission(String permissionId) {
        UserRolePermission permission = new UserRolePermission();
        permission.setId(UUID.randomUUID().toString());
        permission.setRoleId(ROLE_ID);
        permission.setPermissionId(permissionId);
        userRolePermissionMapper.insert(permission);
    }

    private void testSaveBaseInfo(List<SystemParameter> systemParameters, ResultMatcher resultMatcher) throws Exception {
        this.requestPost(BASE_INFO_SAVE_URL, systemParameters, resultMatcher);
    }

    public void testGetBaseInfo(ResultMatcher resultMatcher) throws Exception {
        this.requestGet(BASE_INFO_URL, resultMatcher);
    }


    public void testGetEmailInfo(ResultMatcher resultMatcher) throws Exception {
        this.requestGet(EMAIL_INFO_URL, resultMatcher);
    }

    public void testEditEmailInfo(ResultMatcher resultMatcher) throws Exception {

        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("smtp.host");
                setParamValue("https://baidu.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.port");
                setParamValue("8080");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.account");
                setParamValue("aaa@fit2cloud.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("smtp.ssl");
                setParamValue("true");
                setType("text");
            }});
        }};
        this.requestPost(EMAIL_INFO_SAVE_URL, systemParameters, resultMatcher);
    }


    public void testEmailConnect(ResultMatcher resultMatcher) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("smtp.host", "https://baidu.com");
        hashMap.put("smtp.port", "80");
        hashMap.put("smtp.account", "aaa@fit2cloud.com");
        hashMap.put("smtp.password", "test");
        hashMap.put("smtp.from", "aaa@fit2cloud.com");
        hashMap.put("smtp.recipient", "aaa@fit2cloud.com");
        hashMap.put("smtp.ssl", "ture");
        hashMap.put("smtp.tls", "false");
        this.requestPost(EMAIL_INFO_TEST_CONNECT_URL, hashMap, resultMatcher);
    }

    private List<SystemParameter> getSystemParameters() {
        List<SystemParameter> systemParameters = new ArrayList<>() {{
            add(new SystemParameter() {{
                setParamKey("base.url");
                setParamValue("https://baidu.com");
                setType("text");
            }});
            add(new SystemParameter() {{
                setParamKey("base.prometheus.host");
                setParamValue("http://127.0.0.1:1111");
                setType("text");
            }});
        }};
        return systemParameters;
    }

    private void testAddSource(AuthSourceRequest authSource, ResultMatcher resultMatcher) throws Exception {
        this.requestPost(AUTH_SOURCE_ADD, authSource, resultMatcher);
    }

    public void testGetSourceList(ResultMatcher resultMatcher) throws Exception {
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(10);
        this.requestPost(AUTH_SOURCE_List, basePageRequest, resultMatcher);
    }

    public void testUpdateSource(AuthSourceRequest authSource, ResultMatcher resultMatcher) throws Exception {
        this.requestPost(AUTH_SOURCE_UPDATE, authSource, resultMatcher);
    }

    public void testUpdateStatus(AuthSourceRequest authSource, ResultMatcher resultMatcher) throws Exception {
        this.requestGet(AUTH_SOURCE_UPDATE + "/" + authSource.getId() + "/status/false", resultMatcher);
    }

    public void testGetSourceById(AuthSourceRequest authSource, ResultMatcher resultMatcher) throws Exception {
        this.requestGet(AUTH_SOURCE_GET + authSource.getId(), resultMatcher);
    }

    public void testDelSourceById(AuthSourceRequest authSource, ResultMatcher resultMatcher) throws Exception {
        this.requestGet(AUTH_SOURCE_DELETE + authSource.getId(), resultMatcher);
    }

    private MvcResult requestGet(String url, ResultMatcher resultMatcher) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(resultMatcher).andDo(print()).andReturn();
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
}
