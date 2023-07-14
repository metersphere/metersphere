package base;

import base.param.InvalidateParamInfo;
import base.param.ParamGeneratorFactory;
import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.controller.handler.result.IResultCode;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.system.domain.UserRolePermission;
import io.metersphere.system.mapper.UserRolePermissionMapper;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {
    @Resource
    protected MockMvc mockMvc;
    protected static String sessionId;
    protected static String csrfToken;
    protected static AuthInfo adminAuthInfo;
    protected static Map<String, AuthInfo> permissionAuthInfoMap = new HashMap(3);
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private UserRolePermissionMapper userRolePermissionMapper;

    /**
     * 可以重写该方法定义 BASE_PATH
     */
    protected String getBasePath() {
        return StringUtils.EMPTY;
    }

    @BeforeEach
    public void login() throws Exception {
        if (this.adminAuthInfo == null) {
            this.adminAuthInfo = initAuthInfo("admin", "metersphere");
            this.sessionId = this.adminAuthInfo.getSessionId();
            this.csrfToken = this.adminAuthInfo.getCsrfToken();
        }
        if (permissionAuthInfoMap.isEmpty()) {
            // 获取系统，组织，项目对应的权限测试用户的认证信息
            // 暂时只支持 SYSTEM
            // todo 补充 ORGANIZATION PROJECT
            String permissionType = UserRoleType.SYSTEM.name();
            AuthInfo authInfo = initAuthInfo(permissionType, "metersphere");
            permissionAuthInfoMap.put(permissionType, authInfo);
        }
    }

    private AuthInfo initAuthInfo(String username, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        return new AuthInfo(sessionId, csrfToken);
    }

    private MockHttpServletRequestBuilder getPostRequestBuilder(String url, Object param, Object... uriVariables) {
        return MockMvcRequestBuilders.post(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, adminAuthInfo.getSessionId())
                .header(SessionConstants.CSRF_TOKEN, adminAuthInfo.getCsrfToken())
                .content(JSON.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder getRequestBuilder(String url, Object... uriVariables) {
        return MockMvcRequestBuilders.get(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, adminAuthInfo.getSessionId())
                .header(SessionConstants.CSRF_TOKEN, adminAuthInfo.getCsrfToken());
    }

    protected ResultActions requestPost(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected MvcResult requestPostAndReturn(String url, Object param, Object... uriVariables) throws Exception {
        return this.requestPost(url, param, uriVariables).andReturn();
    }

    protected ResultActions requestGet(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected MvcResult requestGetAndReturn(String url, Object... uriVariables) throws Exception {
        return this.requestGet(url, uriVariables).andReturn();
    }

    protected ResultActions requestGetWithOk(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    protected MvcResult requestGetWithOkAndReturn(String url, Object... uriVariables) throws Exception {
        return this.requestGetWithOk(url, uriVariables).andReturn();
    }

    protected ResultActions requestPostWithOk(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    protected MvcResult requestPostWithOkAndReturn(String url, Object param, Object... uriVariables) throws Exception {
        return this.requestPostWithOk(url, param, uriVariables).andReturn();
    }

    protected <T> T getResultData(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Object data = JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        return JSON.parseObject(JSON.toJSONString(data), clazz);
    }

    protected <T> T getResultMessageDetail(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Object data = JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("messageDetail");
        return JSON.parseObject(JSON.toJSONString(data), clazz);
    }

    protected <T> List<T> getResultDataArray(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Object data = JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        return JSON.parseArray(JSON.toJSONString(data), clazz);
    }

    /**
     * 校验错误响应码
     */
    protected void assertErrorCode(ResultActions resultActions, IResultCode resultCode) throws Exception {
        resultActions
                .andExpect(
                        jsonPath("$.code")
                                .value(resultCode.getCode())
                );
    }

    protected <T> Pager<List<T>> getPageResult(MvcResult mvcResult, Class<T> clazz) throws Exception {
        Map<String, Object> pagerResult = (Map<String, Object>) JSON.parseMap(mvcResult.getResponse().getContentAsString()).get("data");
        List<T> list = JSON.parseArray(JSON.toJSONString(pagerResult.get("list")), clazz);
        Pager pager = new Pager();
        pager.setPageSize(Long.valueOf(pagerResult.get("pageSize").toString()));
        pager.setCurrent(Long.valueOf(pagerResult.get("current").toString()));
        pager.setTotal(Long.valueOf(pagerResult.get("total").toString()));
        pager.setList(list);
        return pager;
    }

    protected void checkLog(String resourceId, OperationLogType operationLogType) throws Exception {
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andSourceIdEqualTo(resourceId).andTypeEqualTo(operationLogType.name());
        operationLogMapper.selectByExample(example).stream()
                .filter(operationLog -> operationLog.getSourceId().equals(resourceId))
                .filter(operationLog -> operationLog.getType().equals(operationLogType.name()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getProjectId()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getModule()))
                .findFirst()
                .orElseThrow(() -> new Exception("日志不存在，请补充操作日志"));
    }

    /**
     * Created 分组参数校验
     */
    protected <T> void createdGroupParamValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(Created.class, clazz, path);
    }

    /**
     * Updated 分组参数校验
     */
    protected <T> void updatedGroupParamValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(Updated.class, clazz, path);
    }

    /**
     * 没有分组的参数校验
     */
    protected <T> void paramValidateTest(Class<T> clazz, String path) throws Exception {
        paramValidateTest(null, clazz, path);
    }

    /**
     * 根据指定的参数定义，自动生成异常参数，进行参数校验的测试
     *
     * @param group 分组
     * @param clazz 参数类名
     * @param path  请求路径
     * @param <T>   参数类型
     * @throws Exception
     */
    private <T> void paramValidateTest(Class group, Class<T> clazz, String path) throws Exception {
        System.out.println("paramValidateTest-start: ====================================");
        System.out.println("url: " + getBasePath() + path);

        List<InvalidateParamInfo> invalidateParamInfos = ParamGeneratorFactory.generateInvalidateParams(group, clazz);
        for (InvalidateParamInfo invalidateParamInfo : invalidateParamInfos) {
            System.out.println("valid: " + invalidateParamInfo.getName() + " " + invalidateParamInfo.getAnnotation().getSimpleName());
            System.out.println("param: " + JSON.toJSONString(invalidateParamInfo.getValue()));
            MvcResult mvcResult = this.requestPostAndReturn(path, invalidateParamInfo.getValue());
            // 校验错误是否是参数错误
            Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
            Map messageDetail = getResultMessageDetail(mvcResult, Map.class);
            System.out.println("result: " + messageDetail);
            // 校验错误信息中包含了该字段
            Assertions.assertTrue(messageDetail.containsKey(invalidateParamInfo.getName()));
        }
        System.out.println("paramValidateTest-end: ====================================");
    }

    protected void requestPostPermissionTest(String permissionId, String url, Object param, Object... uriVariables) throws Exception {
        requestPermissionTest(permissionId, url, () -> getPermissionPostRequestBuilder(permissionId, url, param, uriVariables));
    }

    /**
     * 校验权限
     * 实现步骤
     * 1. 在 application.properties 配置权限的初始化 sql
     *      spring.sql.init.mode=always
     *      spring.sql.init.schema-locations=classpath*:dml/init_permission_test.sql
     * 2. 在 init_permission_test.sql 中配置权限，
     *    初始化名称为 permissionId 前缀（SYSTEM, ORGANIZATION, PROJECT）的用户组和用户，并关联
     * 3. 向该用户组中添加权限测试是否生效，删除权限测试是否可以访问
     * @param permissionId
     * @param url
     * @param requestBuilderGetFunc 请求构造器，一个 builder 只能使用一次，需要重新生成
     * @throws Exception
     */
    private void requestPermissionTest(String permissionId, String url, Supplier<MockHttpServletRequestBuilder> requestBuilderGetFunc)  throws Exception {
        String roleId = permissionId.split("_")[0];
        // 先给初始化的用户组添加权限
        UserRolePermission userRolePermission = initUserRolePermission(roleId, permissionId);

        // 添加后刷新下权限
        refreshUserPermission(permissionId);

        int status = mockMvc.perform(requestBuilderGetFunc.get())
                .andReturn()
                .getResponse()
                .getStatus();

        // 校验是否有权限
        if (status == HttpStatus.FORBIDDEN.value()) {
            throw new MSException(String.format("接口 %s 权限校验失败 %s", getBasePath() + url, permissionId));
        }

        // 删除权限
        userRolePermissionMapper.deleteByPrimaryKey(userRolePermission.getId());

        // 删除后刷新下权限
        refreshUserPermission(permissionId);

        // 删除权限后，调用接口，校验是否没有权限
        status = mockMvc.perform(requestBuilderGetFunc.get())
                .andReturn()
                .getResponse()
                .getStatus();

        if (status != HttpStatus.FORBIDDEN.value()) {
            throw new MSException(String.format("接口 %s 没有设置权限 %s", getBasePath() + url, permissionId));
        }
    }

    /**
     * 调用 is-login 接口刷新权限
     * @param permissionId
     * @throws Exception
     */
    private void refreshUserPermission(String permissionId) throws Exception {
        AuthInfo authInfo = getPermissionAuthInfo(permissionId);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/is-login")
                .header(SessionConstants.HEADER_TOKEN, authInfo.getSessionId())
                .header(SessionConstants.CSRF_TOKEN, authInfo.getCsrfToken());
        mockMvc.perform(requestBuilder);
    }

    protected void requestGetPermissionTest(String permissionId, String url, Object... uriVariables) throws Exception {
        requestPermissionTest(permissionId, url, () -> getPermissionRequestBuilder(permissionId, url, uriVariables));
    }

    /**
     * 给用户组绑定对应权限
     * @param roleId
     * @param permissionId
     * @return
     */
    private UserRolePermission initUserRolePermission(String roleId, String permissionId) {
        UserRolePermission userRolePermission = new UserRolePermission();
        userRolePermission.setRoleId(roleId);
        userRolePermission.setId(UUID.randomUUID().toString());
        userRolePermission.setPermissionId(permissionId);
        userRolePermissionMapper.insert(userRolePermission);
        return userRolePermission;
    }

    private MockHttpServletRequestBuilder getPermissionPostRequestBuilder(String permissionId, String url, Object param, Object... uriVariables) {
        AuthInfo authInfo = getPermissionAuthInfo(permissionId);
        return MockMvcRequestBuilders.post(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, authInfo.getSessionId())
                .header(SessionConstants.CSRF_TOKEN, authInfo.getCsrfToken())
                .content(JSON.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON);
    }

    private AuthInfo getPermissionAuthInfo(String permissionId) {
        return permissionAuthInfoMap.get(permissionId.split("_")[0]);
    }

    private MockHttpServletRequestBuilder getPermissionRequestBuilder(String permissionId, String url, Object... uriVariables) {
        AuthInfo authInfo = getPermissionAuthInfo(permissionId);
        return MockMvcRequestBuilders.get(getBasePath() + url, uriVariables)
                .header(SessionConstants.HEADER_TOKEN, authInfo.getSessionId())
                .header(SessionConstants.CSRF_TOKEN, authInfo.getCsrfToken());
    }

    public String getSessionId() {
        return adminAuthInfo.getSessionId();
    }

    public String getCsrfToken() {
        return adminAuthInfo.getCsrfToken();
    }

    @Data
    class AuthInfo {
        private String sessionId;
        private String csrfToken;

        public AuthInfo(String sessionId, String csrfToken) {
            this.sessionId = sessionId;
            this.csrfToken = csrfToken;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getCsrfToken() {
            return csrfToken;
        }
    }
}
