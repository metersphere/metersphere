package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.UserKey;
import io.metersphere.system.domain.UserKeyExample;
import io.metersphere.system.dto.UserKeyDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserKeyMapper;
import jakarta.annotation.Resource;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiKeysControllerTests extends BaseTest {

    private static String userKeyId;
    private final String BASE_URL = "/user/api/key";
    private final String ADD = BASE_URL + "/add";
    private final String DELETE = BASE_URL + "/delete/%s";
    private final String UPDATE = BASE_URL + "/update";
    private final String LIST = BASE_URL + "/list";
    private final String VALIDATE = BASE_URL + "/validate";
    private final String ENABLE = BASE_URL + "/enable/%s";
    private final String DISABLE = BASE_URL + "/disable/%s";
    @Resource
    private MockMvc mockMvc;

    @Resource
    private UserKeyMapper userKeyMapper;

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAdd() throws Exception {
        requestGet(ADD);
        UserKeyExample userKeyExample = new UserKeyExample();
        userKeyExample.createCriteria().andCreateUserEqualTo("admin");
        List<UserKey> userKeys = userKeyMapper.selectByExample(userKeyExample);
        userKeyId = userKeys.getFirst().getId();
        Assertions.assertEquals(1, userKeys.size());
        //校验日志
        checkLog(userKeys.getFirst().getId(), OperationLogType.ADD);
        //校验只能加五条的限制
        requestGet(ADD);
        requestGet(ADD);
        requestGet(ADD);
        requestGet(ADD);
        Assertions.assertEquals(5, userKeyMapper.countByExample(userKeyExample));
        requestGet(ADD, status().is5xxServerError());
        //用户不存在的
        UserDTO userDTO = new UserDTO();
        userDTO.setId("test-api-keys");
        SessionUser sessionUser = SessionUser.fromUser(userDTO, sessionId);
        mockMvc.perform(MockMvcRequestBuilders.get(ADD).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.ATTR_USER, sessionUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        //校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_API_KEY_ADD, ADD);

    }

    @Test
    @Order(2)
    public void testDelete() throws Exception {
        UserKeyExample userKeyExample = new UserKeyExample();
        userKeyExample.createCriteria().andCreateUserEqualTo("admin");
        List<UserKey> userKeys = userKeyMapper.selectByExample(userKeyExample);
        //选一个id不等于userKeyId的  只要id
        //取所有的id
        List<String> list = userKeys.stream().map(UserKey::getId).filter(id -> !id.equals(userKeyId)).toList();
        requestGet(String.format(DELETE, list.getFirst()));
        Assertions.assertEquals(4, userKeyMapper.countByExample(userKeyExample));
        //校验日志
        checkLog(list.getFirst(), OperationLogType.DELETE);
        //处理不存在的
        requestGet(String.format(DELETE, UUID.randomUUID().toString()), status().is5xxServerError());
        //校验权限
        // requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_API_KEY_DELETE, DELETE, "test-api-keys");
    }

    @Test
    @Order(3)
    public void testUpdate() throws Exception {
        UserKeyDTO userKeyDTO = new UserKeyDTO();
        userKeyDTO.setId(userKeyId);
        userKeyDTO.setDescription("test");
        userKeyDTO.setForever(true);
        this.requestPost(UPDATE, userKeyDTO);
        UserKey org = userKeyMapper.selectByPrimaryKey(userKeyId);
        Assertions.assertEquals("test", org.getDescription());
        Assertions.assertEquals(true, org.getForever());
        userKeyDTO.setForever(false);
        //到期时间为空
        this.requestPost(UPDATE, userKeyDTO, status().is5xxServerError());
        userKeyDTO.setExpireTime(System.currentTimeMillis() - 1000000);
        this.requestPost(UPDATE, userKeyDTO);
        //描述为空
        userKeyDTO.setDescription(null);
        this.requestPost(UPDATE, userKeyDTO);

        //校验不存在的
        userKeyDTO.setId(UUID.randomUUID().toString());
        this.requestPost(UPDATE, userKeyDTO, status().is5xxServerError());
        //校验日志
        checkLog(userKeyId, OperationLogType.UPDATE);
    }

    @Test
    @Order(4)
    public void testList() throws Exception {
        MvcResult mvcResult = this.requestGetAndReturn(LIST);
        List<UserKey> userKeys = parseObjectFromMvcResult(mvcResult, List.class);
        assert userKeys != null;
        Assertions.assertEquals(4, userKeys.size());
        requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_API_KEY_READ, LIST);
    }

    @Test
    @Order(5)
    public void testDisable() throws Exception {
        requestGet(String.format(DISABLE, userKeyId));
        UserKey userKey = userKeyMapper.selectByPrimaryKey(userKeyId);
        Assertions.assertEquals(false, userKey.getEnable());
        //校验日志
        checkLog(userKeyId, OperationLogType.UPDATE);
        //校验不存在的
        requestGet(String.format(DISABLE, UUID.randomUUID().toString()), status().is5xxServerError());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(String.format("{\"username\":\"%s\",\"password\":\"%s\"}", "test-user-key", "test-user-key@metersphere.io"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        String csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");

        mockMvc.perform(MockMvcRequestBuilders.get(String.format(DISABLE, userKeyId))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        // requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_API_KEY_UPDATE, DISABLE, "test-user-key");
    }

    @Test
    @Order(6)
    public void testEnable() throws Exception {
        requestGet(String.format(ENABLE, userKeyId));
        UserKey userKey = userKeyMapper.selectByPrimaryKey(userKeyId);
        Assertions.assertEquals(true, userKey.getEnable());
        //校验日志
        checkLog(userKeyId, OperationLogType.UPDATE);
        //校验不存在的
        requestGet(String.format(ENABLE, UUID.randomUUID().toString()), status().is5xxServerError());
        //requestGetPermissionTest(PermissionConstants.SYSTEM_PERSONAL_API_KEY_UPDATE, ENABLE, "test-user-key");
    }

    @Test
    @Order(7)
    public void testValidateError() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(VALIDATE);
        UserKey userKey = userKeyMapper.selectByPrimaryKey(userKeyId);
        String signature = CodingUtils.aesEncrypt(userKey.getAccessKey() + "|" + UUID.randomUUID().toString() + "|" + System.currentTimeMillis(), userKey.getSecretKey(), userKey.getAccessKey());
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header(SessionConstants.ACCESS_KEY, userKey.getAccessKey())
                .header(SessionConstants.SIGNATURE, signature);
        mockMvc.perform(requestBuilder).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(9)
    public void testValidate() throws Exception {
        List<UserKey> userKeys = userKeyMapper.selectByExample(new UserKeyExample());
        List<String> list = userKeys.stream().map(UserKey::getId).filter(id -> !id.equals(userKeyId)).toList();
        UserKey userKey1 = userKeyMapper.selectByPrimaryKey(list.getFirst());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(VALIDATE);
        String signature = CodingUtils.aesEncrypt(userKey1.getAccessKey() + "|" + UUID.randomUUID().toString() + "|" + System.currentTimeMillis(), userKey1.getSecretKey(), userKey1.getAccessKey());
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header(SessionConstants.ACCESS_KEY, userKey1.getAccessKey())
                .header(SessionConstants.SIGNATURE, signature);
        mockMvc.perform(requestBuilder).andExpect(status().isOk());

        this.adminAuthInfo = null;

    }


}
