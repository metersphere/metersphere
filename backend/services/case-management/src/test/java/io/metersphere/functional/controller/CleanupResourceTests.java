package io.metersphere.functional.controller;

import io.metersphere.functional.service.CleanupCaseReviewResourceService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupResourceTests {
    @Resource
    private CleanupCaseReviewResourceService resourceService;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_clean_review_resource_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testCleanupResource() throws Exception {
        resourceService.deleteResources("test");
        resourceService.deleteResources("project_clean_gyq");
    }

}
