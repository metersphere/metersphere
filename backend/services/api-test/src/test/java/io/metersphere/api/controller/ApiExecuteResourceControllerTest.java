package io.metersphere.api.controller;

import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-13  15:59
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiExecuteResourceControllerTest extends BaseTest {

    private static final String BASE_PATH = "/api/execute/resource/";
    private static final String SCRIPT = "script?reportId={0}&testId={1}";

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void getScript() throws Exception {
        this.requestGetWithOk(SCRIPT, "reportId", "testId");
    }
}
