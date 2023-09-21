package io.metersphere.system.controller;

import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.mock.CreateTestResourceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CreateResourceTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CreateTestResourceService createTestResourceService;

    @Autowired
    public CreateResourceTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    public void testCreateResource() {
        serviceInvoker.invokeCreateServices("test");
        createTestResourceService.createResources("test");
    }

}
