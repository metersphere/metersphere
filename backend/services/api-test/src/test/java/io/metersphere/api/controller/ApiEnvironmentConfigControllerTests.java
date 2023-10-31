package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.api.domain.ApiEnvironmentConfigExample;
import io.metersphere.api.mapper.ApiEnvironmentConfigMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiEnvironmentConfigControllerTests extends BaseTest {
    private final static String add = "/api/definition/env/add/";
    private final static String get = "/api/definition/env/get/";
    private static String envId;
    @Resource
    private MockMvc mockMvc;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiEnvironmentConfigMapper apiEnvironmentConfigMapper;
    private Environment environment;

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

    @BeforeEach
    public void initData() {
        //生成环境
        if (environment == null) {
            Environment env = new Environment();
            env.setId(IDGenerator.nextStr());
            env.setName("test-env-name");
            env.setProjectId(DEFAULT_PROJECT_ID);
            env.setCreateUser("admin");
            env.setCreateTime(System.currentTimeMillis());
            env.setUpdateUser("admin");
            env.setUpdateTime(System.currentTimeMillis());
            environmentMapper.insertSelective(env);
            environment = environmentMapper.selectByPrimaryKey(env.getId());
        }

    }

    private MvcResult responseGet(String url, String projectId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(1)
    public void addDataTestSuccess() throws Exception {
        this.responseGet(add + environment.getId(), DEFAULT_PROJECT_ID);
        ApiEnvironmentConfigExample example = new ApiEnvironmentConfigExample();
        example.createCriteria().andCreateUserEqualTo("admin");
        List<ApiEnvironmentConfig> apiEnvironmentConfigs = apiEnvironmentConfigMapper.selectByExample(example);
        Assertions.assertEquals(apiEnvironmentConfigs.get(0).getEnvironmentId(), environment.getId());
        envId = apiEnvironmentConfigs.get(0).getEnvironmentId();
        //检查日志
        checkLog(apiEnvironmentConfigs.get(0).getId(), OperationLogType.ADD, add);

    }

    @Test
    @Order(2)
    public void getDataTestFail() throws Exception {
        MvcResult mvcResult = responseGet(get + DEFAULT_PROJECT_ID, DEFAULT_PROJECT_ID);
        ApiEnvironmentConfig environmentConfig = parseObjectFromMvcResult(mvcResult, ApiEnvironmentConfig.class);
        Assertions.assertNotNull(environmentConfig);
        Assertions.assertEquals(environmentConfig.getEnvironmentId(), envId);

        mvcResult = responseGet(get + "1111111", "1111111");
        environmentConfig = parseObjectFromMvcResult(mvcResult, ApiEnvironmentConfig.class);
        Assertions.assertNull(environmentConfig);

        responseGet(add + "env-1111", DEFAULT_PROJECT_ID);
        ApiEnvironmentConfigExample example = new ApiEnvironmentConfigExample();
        example.createCriteria().andCreateUserEqualTo("admin");
        List<ApiEnvironmentConfig> apiEnvironmentConfigs = apiEnvironmentConfigMapper.selectByExample(example);
        Assertions.assertEquals(apiEnvironmentConfigs.get(0).getEnvironmentId(), "env-1111");

    }


}
