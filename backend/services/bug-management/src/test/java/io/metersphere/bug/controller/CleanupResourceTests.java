package io.metersphere.bug.controller;

import io.metersphere.bug.service.CleanupBugResourceService;
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
    private CleanupBugResourceService resourceService;

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        resourceService.deleteResources("test");
    }

}
