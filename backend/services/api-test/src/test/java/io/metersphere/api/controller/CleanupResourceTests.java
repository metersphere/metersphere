package io.metersphere.api.controller;

import io.metersphere.api.service.CleanupAPIResourceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupResourceTests {
    @Resource
    private CleanupAPIResourceService resourceService;

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        resourceService.deleteResources("test");
    }

    @Test
    @Order(1)
    public void testCleanupReportResource() throws Exception {
        resourceService.cleanReportResources("test");
    }
}
