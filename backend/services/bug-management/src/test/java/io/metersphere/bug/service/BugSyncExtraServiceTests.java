package io.metersphere.bug.service;

import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.service.CommonFileService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugSyncExtraServiceTests extends BaseTest {

    @Resource
    Platform platform;
    @MockBean
    MinioRepository minioMock;
    @Resource
    private BugAttachmentService bugAttachmentService;
    @Resource
    private CommonFileService commonFileService;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_bug_sync_extra.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void test() throws Exception {
        List<BugFileDTO> allBugFile = bugAttachmentService.getAllBugFiles("bug-for-sync-extra");
        // Mock minio upload exception
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        Mockito.doThrow(new MSException("save minio error!")).when(minioMock).saveFile(Mockito.eq(file), Mockito.any());
        MSException uploadException = assertThrows(MSException.class, () -> commonFileService.uploadTempImgFile(file));
        assertEquals(uploadException.getMessage(), Translator.get("file_upload_fail"));
        // Mock minio delete exception
        Mockito.doThrow(new MSException("delete minio error!")).when(minioMock).delete(Mockito.any());
        MSException deleteException = assertThrows(MSException.class, () ->
                bugAttachmentService.deleteSyncAttachmentFromMs(Set.of("sync-extra-file-associate-B", "sync-extra-file-local-B", "sync-extra-file-local-A.txt"),
                        allBugFile, "bug-for-sync-extra", "project-for-sync-extra"));
        assertEquals(deleteException.getMessage(), "delete minio error!");
        // Reset minio mock
        Mockito.reset(minioMock);
        bugAttachmentService.deleteSyncAttachmentFromMs(Set.of("sync-extra-file-associate-B", "sync-extra-file-local-B"),
                allBugFile, "bug-for-sync-extra", "project-for-sync-extra");

        // Mock null input stream and exception input stream
        Mockito.doAnswer(invocation -> {
            String fileKey = invocation.getArgument(0);
            Consumer<InputStream> inputStreamHandler = invocation.getArgument(1);
            if ("TEST-1".equals(fileKey)) {
                inputStreamHandler.accept(null);
            } else {
                InputStream mockExceptionStream = Mockito.mock(InputStream.class);
                Mockito.doThrow(new MSException("read bytes exception occurred!")).when(mockExceptionStream).readAllBytes();
                inputStreamHandler.accept(mockExceptionStream);
            }
            return null;
        }).when(platform).getAttachmentContent(Mockito.anyString(), Mockito.any());
        // called twice for cover test
        bugAttachmentService.saveSyncAttachmentToMs(platform, "bug-for-sync-extra", "sync-extra-file-associate-B", "TEST-1", "project-for-sync-extra");
        MSException msException = assertThrows(MSException.class, () ->
                bugAttachmentService.saveSyncAttachmentToMs(platform, "bug-for-sync-extra", "sync-extra-file-associate-B", "TEST-2", "project-for-sync-extra"));
        assertEquals(msException.getMessage(), "read bytes exception occurred!");
    }
}
