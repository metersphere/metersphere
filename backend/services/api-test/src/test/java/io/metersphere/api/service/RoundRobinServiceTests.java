package io.metersphere.api.service;

import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class RoundRobinServiceTests {

    @Resource
    private RoundRobinService roundRobinService;

    @Test
    @Order(1)
    public void testInit() throws Exception {
        List<TestResourceNodeDTO> nodes = new LinkedList<>();
        nodes.add(new TestResourceNodeDTO("172.0.0.1", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.2", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.3", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.4", "8080", 10, 3));

        roundRobinService.initializeNodes("test", nodes);
    }

    @Test
    @Order(2)
    public void testGetNode() throws Exception {
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test"));
    }

    @Test
    @Order(3)
    public void testInitAfter() throws Exception {
        List<TestResourceNodeDTO> nodes = new LinkedList<>();
        nodes.add(new TestResourceNodeDTO("172.0.0.1", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.2", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.3", "8080", 10, 3));
        roundRobinService.initializeNodes("test", nodes);

        nodes.add(new TestResourceNodeDTO("172.0.0.3", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.7", "8080", 10, 3));
        nodes.add(new TestResourceNodeDTO("172.0.0.6", "8080", 10, 3));
        roundRobinService.initializeNodes("test", nodes);

    }

    @Test
    @Order(4)
    public void testGetNodeAfter() throws Exception {
        LogUtils.info(roundRobinService.getNextNode("test1"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test2"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test3"));
        LogUtils.info(roundRobinService.getNextNode("test"));
        LogUtils.info(roundRobinService.getNextNode("test1"));
    }
}
