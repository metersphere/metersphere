package io.metersphere.system.controller;

import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.file.MinioRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseDisplayControllerTests extends BaseTest {

    public static final String DISPLAY_ICON = "/display/get/icon";
    public static final String DISPLAY_LOGINIMAGE = "/display/get/loginImage";
    public static final String DISPLAY_LOGINLOGO = "/display/get/loginLogo";
    public static final String DISPLAY_LOGOPLATFORM = "/display/get/logoPlatform";


    //默认
    @Test
    @Order(1)
    public void getIconDefault() throws Exception {
        this.requestGet(DISPLAY_ICON).andDo(print());
    }

    @Test
    @Order(2)
    public void getLoginImageDefault() throws Exception {
        this.requestGet(DISPLAY_LOGINIMAGE).andDo(print());
    }

    @Test
    @Order(3)
    public void getLoginLogoDefault() throws Exception {
        this.requestGet(DISPLAY_LOGINLOGO).andDo(print());
    }

    @Test
    @Order(4)
    public void getLogoPlatformDefault() throws Exception {
        this.requestGet(DISPLAY_LOGOPLATFORM).andDo(print());
    }


    //自定义
    @Test
    @Order(5)
    @Sql(scripts = {"/dml/init_display_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getIcon() throws Exception {
        this.requestGet(DISPLAY_ICON).andDo(print());
    }

    @Test
    @Order(6)
    public void getLoginImage() throws Exception {
        this.requestGet(DISPLAY_LOGINIMAGE).andDo(print());
    }

    @Test
    @Order(7)
    public void getLoginLogo() throws Exception {
        this.requestGet(DISPLAY_LOGINLOGO).andDo(print());
    }

    @Test
    @Order(8)
    public void getLogoPlatform() throws Exception {
        this.requestGet(DISPLAY_LOGOPLATFORM).andDo(print());
    }

}
