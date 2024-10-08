package io.metersphere.api.k8s;

import io.metersphere.engine.EngineFactory;
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class KubernetesExecTests extends BaseTest {
    @Test
    @Order(0)
    public void debugApi() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        TestResourceDTO resource = new TestResourceDTO();
        EngineFactory.debugApi(request, resource);
    }

    @Test
    @Order(1)
    public void runApi() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO();
        TestResourceDTO resource = new TestResourceDTO();
        EngineFactory.runApi(request, resource);
    }


    @Test
    @Order(2)
    public void batchRunApi() throws Exception {
        TaskBatchRequestDTO request = new TaskBatchRequestDTO();
        TestResourceDTO resource = new TestResourceDTO();
        EngineFactory.batchRunApi(request, resource);
    }


    @Test
    @Order(3)
    public void stop() throws Exception {
        List<String> request = new ArrayList<>();
        TestResourceDTO resource = new TestResourceDTO();
        EngineFactory.stopApi(request, resource);
    }


}