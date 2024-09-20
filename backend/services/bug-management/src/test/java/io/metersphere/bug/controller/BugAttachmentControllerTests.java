package io.metersphere.bug.controller;

import io.metersphere.bug.dto.request.BugDeleteFileRequest;
import io.metersphere.bug.dto.request.BugFileSourceRequest;
import io.metersphere.bug.dto.request.BugFileTransferRequest;
import io.metersphere.bug.dto.request.BugUploadFileRequest;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugAttachmentControllerTests extends BaseTest {

    @Resource
    private FileMetadataService fileMetadataService;
    public static final String BUG_ATTACHMENT_LIST = "/bug/attachment/list";
    public static final String BUG_ATTACHMENT_RELATED_PAGE = "/bug/attachment/file/page";
    public static final String BUG_ATTACHMENT_UPLOAD = "/bug/attachment/upload";
    public static final String BUG_ATTACHMENT_DELETE = "/bug/attachment/delete";
    public static final String BUG_ATTACHMENT_PREVIEW = "/bug/attachment/preview";
    public static final String BUG_ATTACHMENT_DOWNLOAD = "/bug/attachment/download";
    public static final String BUG_ATTACHMENT_TRANSFER_OPTION = "/bug/attachment/transfer/options";
    public static final String BUG_ATTACHMENT_TRANSFER = "/bug/attachment/transfer";
    public static final String BUG_ATTACHMENT_CHECK_UPDATE = "/bug/attachment/check-update";
    public static final String BUG_ATTACHMENT_UPDATE = "/bug/attachment/update";
    public static final String BUG_ATTACHMENT_UPLOAD_MD = "/bug/attachment/upload/md/file";
    public static final String BUG_ATTACHMENT_PREVIEW_MD = "/bug/attachment/preview/md";

    @Test
    @Order(0)
    void testUploadMdFile() throws Exception {
        MockMultipartFile fileTooLarge = new MockMultipartFile("file", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[50 * 1024 * 1024 + 1]);
        this.requestUploadFile(BUG_ATTACHMENT_UPLOAD_MD, fileTooLarge).andExpect(status().is2xxSuccessful());
        MockMultipartFile fileWithNoName = new MockMultipartFile("file", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        this.requestUploadFile(BUG_ATTACHMENT_UPLOAD_MD, fileWithNoName).andExpect(status().is5xxServerError());
        // Mock minio save file exception
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        this.requestUploadFile(BUG_ATTACHMENT_UPLOAD_MD, file);
        this.requestGetDownloadFile(BUG_ATTACHMENT_PREVIEW_MD + "/default-project-for-attachment/not-exist-file-id/true", null);
    }

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_bug_attachment.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void prepareData() throws Exception {
        // 准备文件库数据
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId("default-project-for-attachment");
        MockMultipartFile file1 = new MockMultipartFile("file", "TEST1.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        fileMetadataService.upload(fileUploadRequest, "admin", file1);
        MockMultipartFile file2 = new MockMultipartFile("file", "TEST2.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "bb".getBytes());
        fileMetadataService.upload(fileUploadRequest, "admin", file2);
    }

    @Test
    @Order(2)
    void testFilePage() throws Exception {
        List<FileInformationResponse> unRelatedFiles = getRelatedFiles();
        Assertions.assertEquals(2, unRelatedFiles.size());
    }

    @Test
    @Order(3)
    void testUpload() throws Exception {
        List<FileInformationResponse> unRelatedFiles = getRelatedFiles();
        // 非全选关联
        BugUploadFileRequest request = new BugUploadFileRequest();
        request.setBugId("default-attachment-bug-id");
        request.setProjectId("default-project-for-attachment");
        request.setSelectIds(List.of(unRelatedFiles.getFirst().getId()));
        MultiValueMap<String, Object> paramMap1 = getDefaultMultiPartParam(request, null);
        this.requestMultipartWithOk(BUG_ATTACHMENT_UPLOAD, paramMap1);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMapWithFile = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOk(BUG_ATTACHMENT_UPLOAD, paramMapWithFile);
        // 第三方平台的缺陷关联文件
        request.setBugId("default-bug-id-tapd");
        request.setSelectIds(List.of("not-exist-file-id"));
        MultiValueMap<String, Object> paramMap4 = getDefaultMultiPartParam(request, null);
        this.requestMultipart(BUG_ATTACHMENT_UPLOAD, paramMap4);
        request.setSelectIds(List.of(unRelatedFiles.getFirst().getId()));
        MultiValueMap<String, Object> paramMap5 = getDefaultMultiPartParam(request, null);
        this.requestMultipart(BUG_ATTACHMENT_UPLOAD, paramMap5);
        MultiValueMap<String, Object> paramMap6 = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ATTACHMENT_UPLOAD, paramMap6);
    }

    @Test
    @Order(4)
    void previewOrDownload() throws Exception {
        BugFileSourceRequest request = new BugFileSourceRequest();
        request.setBugId("default-attachment-bug-id");
        request.setProjectId("default-project-for-attachment");
        request.setAssociated(false);
        request.setFileId("not-exist-file-id");
        this.requestPostDownloadFile(BUG_ATTACHMENT_PREVIEW, null, request);
        List<BugFileDTO> files = getBugFiles("default-attachment-bug-id");
        files.forEach(file -> {
            request.setFileId(file.getFileId());
            request.setAssociated(!file.getLocal());
            try {
                this.requestPostDownloadFile(BUG_ATTACHMENT_PREVIEW, null, request);
                this.requestPostDownloadFile(BUG_ATTACHMENT_DOWNLOAD, null, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Order(5)
    void testTransfer() throws Exception {
        this.requestGetWithOk(BUG_ATTACHMENT_TRANSFER_OPTION + "/default-project-for-attachment");
        BugFileTransferRequest request = new BugFileTransferRequest();
        request.setBugId("default-attachment-bug-id");
        request.setProjectId("default-project-for-attachment");
        request.setModuleId("root");
        request.setAssociated(false);
        request.setFileId("not-exist-file-id");
        request.setFileName(UUID.randomUUID().toString());
        this.requestPost(BUG_ATTACHMENT_TRANSFER, request).andExpect(status().is5xxServerError());
        List<BugFileDTO> files = getBugFiles("default-attachment-bug-id");
        files.stream().filter(BugFileDTO::getLocal).forEach(file -> {
            request.setFileId(file.getFileId());
            request.setAssociated(!file.getLocal());
            try {
                this.requestPostWithOk(BUG_ATTACHMENT_TRANSFER, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Order(6)
    void testUpgrade() throws Exception {
        // 检查更新
        this.requestPostWithOk(BUG_ATTACHMENT_CHECK_UPDATE, List.of("test-id"));
        List<BugFileDTO> bugFiles = getBugFiles("default-attachment-bug-id");
        BugDeleteFileRequest request = new BugDeleteFileRequest();
        request.setBugId("default-attachment-bug-id");
        request.setProjectId("default-project-for-attachment");
        request.setAssociated(true);
        bugFiles.forEach(file -> {
            try {
                request.setRefId(file.getRefId());
                this.requestPostWithOk(BUG_ATTACHMENT_UPDATE, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        List<BugFileDTO> tapdFiles = getBugFiles("default-bug-id-tapd");
        tapdFiles.stream().filter(file -> !file.getLocal()).forEach(file -> {
            try {
                request.setBugId("default-bug-id-tapd");
                request.setRefId(file.getRefId());
                this.requestPost(BUG_ATTACHMENT_UPDATE, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Order(7)
    void testDelete() throws Exception {
        // Local缺陷附件删除
        List<BugFileDTO> files = getBugFiles("default-attachment-bug-id");
        files.forEach(file -> {
            BugDeleteFileRequest request = new BugDeleteFileRequest();
            request.setBugId("default-attachment-bug-id");
            request.setProjectId("default-project-for-attachment");
            request.setRefId(file.getRefId());
            request.setAssociated(!file.getLocal());
            try {
                this.requestPostWithOk(BUG_ATTACHMENT_DELETE, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        List<BugFileDTO> tapdBugFiles = getBugFiles("default-bug-id-tapd");
        tapdBugFiles.forEach(file -> {
            BugDeleteFileRequest request = new BugDeleteFileRequest();
            request.setBugId("default-bug-id-tapd");
            request.setProjectId("default-project-for-attachment");
            request.setRefId(file.getRefId());
            request.setAssociated(!file.getLocal());
            try {
                this.requestPost(BUG_ATTACHMENT_DELETE, request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<FileInformationResponse> getRelatedFiles() throws Exception {
        FileMetadataTableRequest request = new FileMetadataTableRequest();
        request.setProjectId("default-project-for-attachment");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_ATTACHMENT_RELATED_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回的数据量不超过规定要返回的数据量相同
        return JSON.parseArray(JSON.toJSONString(pageData.getList()), FileInformationResponse.class);
    }

    private List<BugFileDTO> getBugFiles(String bugId) throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_ATTACHMENT_LIST + "/" + bugId);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BugFileDTO.class);
    }
}
