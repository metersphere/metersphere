package io.metersphere.api.service;

import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.sdk.constants.ApiBatchRunMode;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiExecutionQueueServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOps;

    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;

    @Test
    @Order(1)
    void testInsertQueue() {
        ExecutionQueue queue = new ExecutionQueue();
        queue.setQueueId("queueId1");
        ApiRunModeConfigDTO runModeConfig = new ApiRunModeConfigDTO();
        runModeConfig.setRunMode(ApiBatchRunMode.PARALLEL.name());
        runModeConfig.setGrouped(false);
        runModeConfig.setEnvironmentId("envId");
        queue.setRunModeConfig(runModeConfig);
        queue.setCreateTime(System.currentTimeMillis());

        ExecutionQueueDetail queueDetail1 = new ExecutionQueueDetail();
        queueDetail1.setResourceId("resourceId1");
        queueDetail1.setSort(1);

        ExecutionQueueDetail queueDetail2 = new ExecutionQueueDetail();
        queueDetail2.setResourceId("resourceId2");
        queueDetail2.setSort(2);

        List<ExecutionQueueDetail> queueDetails = List.of(queueDetail1, queueDetail2);

        when(redisTemplate.opsForList()).thenReturn(listOps);

        apiExecutionQueueService.insertQueue(queue, queueDetails);
    }

    @Test
    @Order(2)
    void testGetNextDetail() throws Exception {
        String queueId = "queueId1";
        System.out.println("start : " + apiExecutionQueueService.size(queueId));

        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.leftPop(queueId)).thenReturn("{\"resourceId\":\"resourceId1\"}");

        ExecutionQueueDetail result = apiExecutionQueueService.getNextDetail(queueId);

        assertNotNull(result);
        assertEquals("resourceId1", result.getResourceId());

        System.out.println("end : " + apiExecutionQueueService.size(queueId));

    }

    @Test
    @Order(3)
    void testGetDetails() throws Exception {
        String queueId = "queueId1";

        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.size(queueId)).thenReturn(2L);
        when(listOps.index(eq(queueId), anyLong())).thenReturn("{\"resourceId\":\"resourceId1\"}", "{\"resourceId\":\"resourceId2\"}");

        List<ExecutionQueueDetail> result = apiExecutionQueueService.getDetails(queueId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @Order(4)
    void testGetQueue() throws Exception {
        String queueId = "queueId1";
        ExecutionQueue result = apiExecutionQueueService.getQueue(queueId);

        assertNotNull(result);
        assertEquals("queueId1", result.getQueueId());
    }
}
