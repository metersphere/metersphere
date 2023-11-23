package io.metersphere.api.controller;

import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

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
}
