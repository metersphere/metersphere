package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.metersphere.functional.request.FunctionalCaseAssociationFileRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteFileRequest;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.functional.utils.FileBaseUtils;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.file.FileRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseAttachmentControllerTests extends BaseTest {

    private static String FILE_ID = "";

    @Resource
    private FileService fileService;

    @Resource
    private FileMetadataService fileMetadataService;

    public static final String ATTACHMENT_PAGE_URL = "/attachment/page";
    public static final String ATTACHMENT_PREVIEW_URL = "/attachment/preview";
    public static final String ATTACHMENT_DOWNLOAD_URL = "/attachment/download";
    public static final String ATTACHMENT_CHECK_UPDATE_URL = "/attachment/check-update";
    public static final String ATTACHMENT_UPDATE_URL = "/attachment/update/";
    public static final String ATTACHMENT_TRANSFER_URL = "/attachment/transfer";
    public static final String UPLOAD_FILE_URL = "/attachment/upload/file";
    public static final String DELETE_FILE_URL = "/attachment/delete/file";
    public static final String OPTIONS_URL = "/attachment/options/";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_attachment_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
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


    @Test
    @Order(2)
    public void testAttachmentPreview() throws Exception {
        FunctionalCaseFileRequest request = new FunctionalCaseFileRequest();
        request.setProjectId("WX_TEST_PROJECT_ID");
        request.setLocal(true);
        request.setFileId("TEST_ATTACHMENT_ID");
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID");
        uploadLocalFile();
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);

        //覆盖controller
        request.setLocal(false);
        String fileId = uploadFile();
        this.FILE_ID = fileId;
        request.setFileId(fileId);
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);

        //增加覆盖率
        request.setLocal(true);
        request.setProjectId("123213");
        request.setFileId("123123");
        request.setCaseId("123123");
        this.downloadFile(ATTACHMENT_PREVIEW_URL, request);
    }

    private String uploadFile() throws Exception {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        String fileId = fileMetadataService.upload(fileUploadRequest, "admin", file);
        return fileId;
    }

    private void uploadLocalFile() {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName("测试");
        fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir("WX_TEST_PROJECT_ID", "TEST_FUNCTIONAL_CASE_ATTACHMENT_ID") + "/" + "TEST_ATTACHMENT_FILE_ID");
        fileRequest.setStorage(StorageType.MINIO.name());
        try {
            fileService.upload(file, fileRequest);
        } catch (Exception e) {
            throw new MSException("save file error");
        }
    }


    protected MvcResult downloadFile(String url, Object param, Object... uriVariables) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }


    @Test
    @Order(3)
    public void testAttachmentDownload() throws Exception {
        //覆盖controller
        FunctionalCaseFileRequest request = new FunctionalCaseFileRequest();
        request.setProjectId("WX_TEST_PROJECT_ID");
        request.setFileId("TEST_ATTACHMENT_ID");
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID");
        request.setLocal(true);
        this.downloadFile(ATTACHMENT_DOWNLOAD_URL, request);

        request.setLocal(false);
        request.setFileId(FILE_ID);
        this.downloadFile(ATTACHMENT_DOWNLOAD_URL, request);
    }

    @Test
    @Order(4)
    public void testAttachmentCheck() throws Exception {
        //覆盖controller
        this.requestPost(ATTACHMENT_CHECK_UPDATE_URL, Arrays.asList("123", "223", "323"));
    }

    @Test
    @Order(5)
    public void testAttachmentUpdate() throws Exception {
        //覆盖controller
        this.requestGet(ATTACHMENT_UPDATE_URL + DEFAULT_PROJECT_ID + "/wx_test_file_association");
    }

    @Test
    @Order(6)
    public void testAttachmentTransfer() throws Exception {
        //覆盖controller
        FunctionalCaseFileRequest request = new FunctionalCaseFileRequest();
        request.setLocal(false);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setFileId(FILE_ID);
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID");
        this.requestPost(ATTACHMENT_TRANSFER_URL, request);
        request.setFileId("TEST_ATTACHMENT_ID");
        this.requestPost(ATTACHMENT_TRANSFER_URL, request);

    }


    @Test
    @Order(7)
    public void testUploadFile() throws Exception {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        FunctionalCaseAssociationFileRequest request = new FunctionalCaseAssociationFileRequest();
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
        request.setProjectId("WX_TEST_PROJECT_ID");
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipart(UPLOAD_FILE_URL, paramMap);

        request.setFileIds(Arrays.asList("wx_test_file_association_1"));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipart(UPLOAD_FILE_URL, paramMap);

    }

    @Test
    @Order(8)
    public void testDeleteFile() throws Exception {
        FunctionalCaseDeleteFileRequest request = new FunctionalCaseDeleteFileRequest();
        FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
        //覆盖率
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
        request.setProjectId("WX_TEST_PROJECT_ID");
        request.setId("wx_test_file_association_1");
        request.setLocal(false);
        this.requestPost(DELETE_FILE_URL, request);

        attachmentDTO.setId("TEST_ATTACHMENT_ID");
        attachmentDTO.setLocal(true);
        this.requestPost(DELETE_FILE_URL, request);
    }


    @Test
    @Order(9)
    public void testOptions() throws Exception {
        //覆盖controller方法
        this.requestGet(OPTIONS_URL + DEFAULT_PROJECT_ID);
    }

}
