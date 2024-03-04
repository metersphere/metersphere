package io.metersphere.system.service;

import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.utils.TaskRunnerClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskRunnerClientTest extends BaseTest {


    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private Integer mockServerHostPort;

    @Test
    public void debugApi() throws Exception {
        mockPost("/api/debug", "");
        String endpoint = TaskRunnerClient.getEndpoint(mockServerHost, mockServerHostPort.toString());
        TaskRunnerClient.debugApi(endpoint, new TaskRequestDTO());
    }

    @Test
    public void runApi() throws Exception {
        mockPost("/api/run", "");
        String endpoint = TaskRunnerClient.getEndpoint(mockServerHost, mockServerHostPort.toString());
        TaskRunnerClient.runApi(endpoint, new TaskRequestDTO());
    }
}
