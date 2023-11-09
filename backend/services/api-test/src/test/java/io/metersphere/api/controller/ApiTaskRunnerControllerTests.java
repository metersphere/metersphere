package io.metersphere.api.controller;

import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTaskRunnerControllerTests extends BaseTest {
    private static final String BASE_PATH = "/task/runner/get/test-code";

    @Test
    @Order(0)
    public void testCode() throws Exception {
        // @@校验没有数据的情况
        this.requestGetWithOk(BASE_PATH);
    }

}
