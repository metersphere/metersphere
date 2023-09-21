package io.metersphere.project.controller;

import io.metersphere.project.service.CleanupEnvironmentResourceService;
import io.metersphere.system.invoker.ProjectServiceInvoker;
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
public class CleanupEnvironmentTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CleanupEnvironmentResourceService cleanupEnvironmentResourceService;
    @Autowired
    public CleanupEnvironmentTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        serviceInvoker.invokeServices("test");
        cleanupEnvironmentResourceService.cleanReportResources("test");
    }

}
