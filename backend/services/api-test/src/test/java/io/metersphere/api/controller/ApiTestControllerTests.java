package io.metersphere.api.controller;

import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.KeyValueParam;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  17:07
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiTestControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/test/";
    protected static final String PROTOCOL_LIST = "protocol/{0}";
    protected static final String MOCK = "mock/{0}";
    protected static final String CUSTOM_FUNC_RUN = "custom/func/run";

    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void getProtocols() throws Exception {
        // @@请求成功
        this.requestGetWithOk(PROTOCOL_LIST, this.DEFAULT_ORGANIZATION_ID).andReturn();
    }

    @Test
    public void getMock() throws Exception {
        // @@请求成功
        this.requestGetWithOk(MOCK, "@integer").andReturn();
    }

    @Test
    public void runCustomFunc() throws Exception {
        mockPost("/api/debug", "");
        // 初始化资源池
        baseResourcePoolTestService.initProjectResourcePool();

        CustomFunctionRunRequest request = new CustomFunctionRunRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ScriptLanguageType.BEANSHELL.getValue());
        request.setScript("""
                    log.info("========");
                    log.info("${test}");
                """);
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);

        KeyValueParam keyValueParam = new KeyValueParam();
        keyValueParam.setKey("test");
        keyValueParam.setValue("value");
        request.setParams(List.of(keyValueParam));
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_EXECUTE, CUSTOM_FUNC_RUN, request);
    }
}
