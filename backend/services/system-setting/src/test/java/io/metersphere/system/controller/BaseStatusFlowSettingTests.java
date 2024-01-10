package io.metersphere.system.controller;

import io.metersphere.system.service.BaseStatusFlowSettingService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseStatusFlowSettingTests {

    @Resource
    private BaseStatusFlowSettingService baseStatusFlowSettingService;
    @Test
    @Order(99)
    @Sql(scripts = {"/dml/init_status_item.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void coverLocalStatusItem() {
        baseStatusFlowSettingService.getStatusTransitions("default-project-for-no-status", "BUG", null);
        baseStatusFlowSettingService.getStatusTransitions("default-project-for-status", "BUG", null);
        baseStatusFlowSettingService.getStatusTransitions("default-project-for-status", "BUG", "1");
        baseStatusFlowSettingService.getStatusTransitions("default-project-for-status", "BUG", "2");
        baseStatusFlowSettingService.getAllStatusOption("default-project-for-status", "BUG");
    }
}
