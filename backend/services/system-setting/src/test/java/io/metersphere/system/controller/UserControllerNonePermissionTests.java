package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.UserInfo;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.utils.UserTestUtils;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = {"/dml/init_user_controller_none_permission_test.sql"},
        config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerNonePermissionTests {
    @Resource
    private MockMvc mockMvc;

    //涉及到的url
    private static final String URL_USER_CREATE = "/system/user/add";
    private static final String URL_USER_UPDATE = "/system/user/update";
    private static final String URL_USER_GET = "/system/user/get/%s";
    private static final String URL_USER_PAGE = "/system/user/page";
    private static final String URL_GET_GLOBAL_SYSTEM = "/system/user/get/global/system/role";

    protected static String sessionId;
    protected static String csrfToken;
    private static final String NONE_ROLE_USERNAME = "tianyang.member@163.com";
    private static final String NONE_ROLE_PASSWORD = "tianyang.member@163.com";

    private static final ResultMatcher CHECK_RESULT_MATHER = status().isForbidden();

    /**
     * 用户创建接口判断无权限用户是否可以访问
     * 接口参数校验优先级大于操作用户权限校验，所以参数要保证合法化
     */
    @Test
    @Order(0)
    public void testGetGlobalSystemUserRoleSuccess() throws Exception {
        UserInfo paramUserInfo = new UserInfo() {{
            setId("testId");
            setName("tianyang.no.permission.email");
            setEmail("tianyang.no.permission.email@126.com");
            setSource("LOCAL");
        }};
        List<UserRoleOption> paramRoleList = new ArrayList<>() {{
            this.add(
                    new UserRoleOption() {{
                        this.setId("member");
                        this.setName("member");
                    }});
        }};
        this.requestGet(String.format(URL_USER_GET, NONE_ROLE_USERNAME), CHECK_RESULT_MATHER);
        //校验权限：系统全局用户组获取
        this.requestGet(URL_GET_GLOBAL_SYSTEM, CHECK_RESULT_MATHER);
        //校验权限：用户创建
        this.requestPost(URL_USER_CREATE,
                UserTestUtils.getUserCreateDTO(
                        paramRoleList,
                        new ArrayList<>() {{
                            add(paramUserInfo);
                        }}
                ),
                CHECK_RESULT_MATHER);
        //校验权限：分页查询用户列表
        this.requestPost(URL_USER_PAGE, UserTestUtils.getDefaultPageRequest(), CHECK_RESULT_MATHER);
        //校验权限：修改用户
        this.requestPost(URL_USER_UPDATE,
                UserTestUtils.getUserUpdateDTO(paramUserInfo, paramRoleList), CHECK_RESULT_MATHER);
    }

    @BeforeEach
    public void login() throws Exception {
        if (StringUtils.isAnyBlank(sessionId, csrfToken)) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content("{\"username\":\"" + NONE_ROLE_USERNAME + "\",\"password\":\"" + NONE_ROLE_PASSWORD + "\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
            csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        }
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

    private void requestGet(String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
