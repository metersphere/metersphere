package io.metersphere.api.controller;

import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.debug.ApiDebugService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-13  15:59
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiExecuteResourceControllerTest extends BaseTest {

    private static final String BASE_PATH = "/api/execute/resource/";
    private static final String SCRIPT = "script?reportId={0}&testId={1}";
    private static final String FILE = "file?reportId={0}&testId={1}";
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiDebugService apiDebugService;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void getScript() throws Exception {
        this.requestGetWithOk(SCRIPT, "reportId", "testId");
    }

    @Test
    public void downloadFile() throws Exception {
        String fileName = IDGenerator.nextStr() + "_file_upload.JPG";
        MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        String fileId = apiDebugService.uploadTempFile(file);
        FileRequest fileRequest = new FileCopyRequest();
        fileRequest.setFileName(fileName);
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        mockMvc.perform(getPostRequestBuilder(FILE, fileRequest, "reportId", "testId"))
                .andExpect(status().isOk());

        String reportId = UUID.randomUUID().toString();
        String testId = UUID.randomUUID().toString();
        String scriptRedisKey = apiExecuteService.getScriptRedisKey(reportId, testId);
        stringRedisTemplate.opsForValue().set(scriptRedisKey, "aaa");
        mockMvc.perform(getPostRequestBuilder(FILE, fileRequest,  reportId, testId))
                .andExpect(status().isOk());

        fileRequest.setStorage(StorageType.MINIO.name());
        mockMvc.perform(getPostRequestBuilder(FILE, fileRequest,  reportId, testId))
                .andExpect(status().isOk());

    }
}
