package io.metersphere.functional.controller;

import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseAttachmentControllerTests extends BaseTest {

    public static final String ATTACHMENT_PAGE_URL = "/attachment/page";

    @Test
    @Order(1)
    public void testAttachmentPage() throws Exception {
        FileMetadataTableRequest request = new FileMetadataTableRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ATTACHMENT_PAGE_URL, request);
        String updateReturnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateReturnData, ResultHolder.class);
        Assertions.assertNotNull(updateResultHolder);

    }
}
