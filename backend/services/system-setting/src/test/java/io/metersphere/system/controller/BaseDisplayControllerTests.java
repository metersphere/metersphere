package io.metersphere.system.controller;

import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.system.base.BaseTest;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;



@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseDisplayControllerTests extends BaseTest {

    public static final String DISPLAY_ICON = "/base-display/get/icon";
    public static final String DISPLAY_LOGINIMAGE = "/base-display/get/login-image";
    public static final String DISPLAY_LOGINLOGO = "/base-display/get/login-logo";
    public static final String DISPLAY_LOGOPLATFORM = "/base-display/get/logo-platform";

    //默认图片
    @Resource
    private MinioRepository repository;

    @Test
    @Order(1)
    public void getIconDefault() throws Exception {
        this.requestGet(DISPLAY_ICON);
    }

    @Test
    @Order(2)
    public void getLoginImageDefault() throws Exception {
        this.requestGet(DISPLAY_LOGINIMAGE);
    }

    @Test
    @Order(3)
    public void getLoginLogoDefault() throws Exception {
        this.requestGet(DISPLAY_LOGINLOGO);
    }

    @Test
    @Order(4)
    public void getLogoPlatformDefault() throws Exception {
        this.requestGet(DISPLAY_LOGOPLATFORM);
    }

    //文件先放入minio
    @Test
    @Order(6)
    public void saveFile() throws Exception {

        saveImages("/file/favicon.ico","favicon","favicon.ico","ui.icon:favicon.ico");
        saveImages("/file/login-banner.jpg","login-banner","login-banner.jpg","ui.loginImage:login-banner.jpg");
        saveImages("/file/login-logo.svg","login-logo","login-logo.svg","ui.loginLogo:login-logo.svg");
        saveImages("/file/MS-full-logo.svg","MS-full-logo","MS-full-logo.svg","ui.logoPlatform:MS-full-logo.svg");

    }

    private void saveImages(String url, String name, String originalFileName, String fileName) throws Exception{
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        byte[] bytes = IOUtils.toByteArray(resolver.getResource(url).getInputStream());
        MockMultipartFile mockFile = new MockMultipartFile(name, originalFileName, "application/octet-stream", bytes);
        FileRequest request = new FileRequest();
        request.setFileName(fileName);
        request.setFolder(DefaultRepositoryDir.getSystemRootDir());
        repository.saveFile(mockFile, request);
    }


    //取minio中的图片
    @Test
    @Order(5)
    @Sql(scripts = {"/dml/init_display_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getIconError() throws Exception {
        this.requestGet(DISPLAY_ICON);
    }

    //取minio中的图片
    @Test
    @Order(7)
    public void getIcon() throws Exception {
        this.requestGet(DISPLAY_ICON);
    }

    @Test
    @Order(7)
    public void getLoginImage() throws Exception {
        this.requestGet(DISPLAY_LOGINIMAGE);
    }

    @Test
    @Order(7)
    public void getLoginLogo() throws Exception {
        this.requestGet(DISPLAY_LOGINLOGO);
    }

    @Test
    @Order(8)
    public void getLogoPlatform() throws Exception {
        this.requestGet(DISPLAY_LOGOPLATFORM);
    }

    private MvcResult requestGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andReturn();
    }
}
