package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.job.CleanUpReportJob;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
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
public class CleanReportJobTests extends BaseTest {

    @Resource
    private CleanUpReportJob cleanUpReportJob;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void test() throws Exception {
        BooleanUtils.isTrue(null);
        cleanUpReportJob.cleanReport();
    }

}
