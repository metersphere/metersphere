package io.metersphere.project.controller;

import io.metersphere.project.service.CreateApplicationResourceService;
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
public class CreateApplicationResourceTests {

    @Resource
    private CreateApplicationResourceService resourceService;

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        resourceService.createResources("test_project_id");
    }
}
