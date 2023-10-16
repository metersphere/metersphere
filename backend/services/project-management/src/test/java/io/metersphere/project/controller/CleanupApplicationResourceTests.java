package io.metersphere.project.controller;

import io.metersphere.project.service.CleanupApplicationResourceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupApplicationResourceTests {

    @Resource
    private CleanupApplicationResourceService resourceService;

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        resourceService.deleteResources("test_project_id");
    }
}
