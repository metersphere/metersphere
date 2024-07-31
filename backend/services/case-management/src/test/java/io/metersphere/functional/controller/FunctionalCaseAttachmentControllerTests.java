package io.metersphere.functional.controller;

import io.metersphere.functional.constants.CaseFileSourceType;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.functional.utils.FileBaseUtils;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.system.service.FileService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;

    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;

    public static final String ATTACHMENT_PAGE_URL = "/attachment/page";
    public static final String ATTACHMENT_PREVIEW_URL = "/attachment/preview";
    public static final String ATTACHMENT_DOWNLOAD_URL = "/attachment/download";
    public static final String ATTACHMENT_CHECK_UPDATE_URL = "/attachment/check-update";
    public static final String ATTACHMENT_UPDATE_URL = "/attachment/update/";
    public static final String ATTACHMENT_TRANSFER_URL = "/attachment/transfer";
    public static final String UPLOAD_FILE_URL = "/attachment/upload/file";
    public static final String DELETE_FILE_URL = "/attachment/delete/file";
    public static final String OPTIONS_URL = "/attachment/options/";
    public static final String UPLOAD_TEMP = "/attachment/upload/temp/file";
    public static final String UPLOAD_DOWNLOAD = "/attachment/download/file/%s/%s/%S";

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
        request.setFileId("TEST_ATTACHMENT_FILE_ID");
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
        request.setFileId("TEST_ATTACHMENT_FILE_ID");
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
        AttachmentTransferRequest request = new AttachmentTransferRequest();
        request.setLocal(false);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setFileId(FILE_ID);
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID");
        request.setFileName(UUID.randomUUID().toString());
        request.setModuleId("root");
        this.requestPost(ATTACHMENT_TRANSFER_URL, request);
        request.setFileId("TEST_ATTACHMENT_FILE_ID");
        this.requestPost(ATTACHMENT_TRANSFER_URL, request);
        functionalCaseAttachmentService.deleteCaseAttachment(List.of("TEST_ATTACHMENT_FILE_ID"), "TEST_FUNCTIONAL_CASE_ATTACHMENT_ID", "WX_TEST_PROJECT_ID");

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
        //覆盖率
        request.setCaseId("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
        request.setProjectId("WX_TEST_PROJECT_ID");
        request.setId("wx_test_file_association_1");
        request.setLocal(false);
        this.requestPost(DELETE_FILE_URL, request);

        request.setId("TEST_ATTACHMENT_FILE_ID");
        request.setLocal(true);
        this.requestPost(DELETE_FILE_URL, request);
    }


    @Test
    @Order(9)
    public void testOptions() throws Exception {
        //覆盖controller方法
        this.requestGet(OPTIONS_URL + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(10)
    public void testUploadTemp() throws Exception {
        //覆盖controller方法
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        Assertions.assertTrue(StringUtils.isNotBlank(fileId));
        file = getNoNameMockMultipartFile();
        doUploadTempFileFalse(file);
        functionalCaseAttachmentService.uploadMinioFile("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1", "WX_TEST_PROJECT_ID", List.of(fileId), "admin", CaseFileSourceType.CASE_DETAIL.toString());
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1").andFileIdEqualTo(fileId).andFileSourceEqualTo(CaseFileSourceType.CASE_DETAIL.toString());
        List<FunctionalCaseAttachment> functionalCaseAttachments = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(functionalCaseAttachments));
        functionalCaseAttachmentService.uploadMinioFile("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1", "WX_TEST_PROJECT_ID", new ArrayList<>(), "admin", CaseFileSourceType.CASE_COMMENT.toString());
        String functionalCaseDir = DefaultRepositoryDir.getFunctionalCaseDir("WX_TEST_PROJECT_ID", "TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
        functionalCaseAttachmentService.uploadFileResource(functionalCaseDir, new HashMap<>(), "WX_TEST_PROJECT_ID", "TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
        Map<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(fileId, null);
        functionalCaseAttachmentService.uploadFileResource(functionalCaseDir, objectObjectHashMap, "WX_TEST_PROJECT_ID", "TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1");
    }

    @Test
    @Order(11)
    public void downTemp() throws Exception {
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        Assertions.assertTrue(StringUtils.isNotBlank(fileId));
        MvcResult compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", fileId, false));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", fileId, true));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        functionalCaseAttachmentService.uploadMinioFile("TEST_FUNCTIONAL_CASE_ATTACHMENT_ID_1", "WX_TEST_PROJECT_ID", List.of(fileId), "admin", CaseFileSourceType.CASE_DETAIL.toString());
        compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", fileId, false));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", fileId, true));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        MockMultipartFile newFile = getMockMultipartFile();
        String newFileId = uploadTemp(newFile);
        compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", newFileId, true));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
        compressedResult = this.downloadTempFile(String.format(UPLOAD_DOWNLOAD, "WX_TEST_PROJECT_ID", "1123586", true));
        Assertions.assertFalse(compressedResult.getResponse().getContentAsByteArray().length > 0);
    }

    public String uploadTemp(MultipartFile file) {
        String fileName = StringUtils.trim(file.getOriginalFilename());
        String fileId = IDGenerator.nextStr();
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        try {
            FileCenter.getDefaultRepository()
                    .saveFile(file, fileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file_upload_fail"));
        }
        return fileId;
    }

    protected MvcResult downloadTempFile(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    private static MockMultipartFile getMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file_upload.JPG",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }


    private static MockMultipartFile getNoNameMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    private void doUploadTempFileFalse(MockMultipartFile file) throws Exception {
        this.requestUploadFile(UPLOAD_TEMP, file).andExpect(status().is5xxServerError());
    }

}
