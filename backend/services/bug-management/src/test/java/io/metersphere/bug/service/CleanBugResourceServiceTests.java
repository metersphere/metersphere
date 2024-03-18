package io.metersphere.bug.service;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CleanBugResourceServiceTests extends BaseTest {

    private final ProjectServiceInvoker serviceInvoker;

    @Autowired
    public CleanBugResourceServiceTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_bug_clean_resource.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void test() {
        serviceInvoker.invokeServices("default-project-for-clean-resource");
        serviceInvoker.invokeServices("default-project-for-clean-resource-not-exist");
    }
}
