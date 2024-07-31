package io.metersphere.system.service;

import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-07-31  16:25
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommonFileServiceTest extends BaseTest {
    @Resource
    private CommonFileService commonFileService;

    @Test
    public void uploadTempImgFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        Assertions.assertThrows(MSException.class, () -> commonFileService.uploadTempImgFile(file));
    }

    @Test
    public void saveFileFromTempFile() throws Exception {
        // 测试方法正确
        MockMultipartFile file = new MockMultipartFile("file", IDGenerator.nextStr() + "_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        String fileId = commonFileService.uploadTempImgFile(file);
        String orgTemplateImgDir = DefaultRepositoryDir.getOrgTemplateImgDir(DEFAULT_ORGANIZATION_ID);
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put(fileId, file.getOriginalFilename());
        commonFileService.saveFileFromTempFile(orgTemplateImgDir, fileMap);
        // 校验文件是否存在
        assertUploadFile(fileId, file.getOriginalFilename());

        // 增加 key 为 null 覆盖率
        fileMap = new HashMap<>();
        fileMap.put(fileId, null);
        commonFileService.saveFileFromTempFile(orgTemplateImgDir, fileMap);
        commonFileService.saveFileFromTempFile(orgTemplateImgDir, new HashMap<>());
        commonFileService.saveReviewImgFromTempFile(orgTemplateImgDir, orgTemplateImgDir, fileMap);
        commonFileService.saveReviewImgFromTempFile(orgTemplateImgDir, orgTemplateImgDir, new HashMap<>());

        // 校验文件不存在异常
        fileMap = new HashMap<>();
        fileMap.put("no id", file.getOriginalFilename());
        Map<String, String> finalFileMap = fileMap;
        Assertions.assertThrows(MSException.class, () -> commonFileService.saveFileFromTempFile(orgTemplateImgDir, finalFileMap));
        Map<String, String> finalFileMap2 = fileMap;
        Assertions.assertThrows(MSException.class, () -> commonFileService.saveReviewImgFromTempFile(orgTemplateImgDir, orgTemplateImgDir, finalFileMap2));
    }

    @Test
    public void moveTempFileToImgReviewFolder() throws Exception {
        // 增加非图片类型文件的覆盖率
        commonFileService.moveTempFileToImgReviewFolder("" , "", "test.txt");
    }

    @Test
    public void getFileNameByFileId() {
        // 增加文件不存在覆盖率
        commonFileService.getFileNameByFileId("111" , "");
    }

    @Test
    public void uploadReviewImg() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        commonFileService.uploadReviewImg(file, "", "");
    }

    /**
     * 校验上传的文件
     *
     */
    public static void assertUploadFile(String fileId, String fileName) throws Exception {
        String orgTemplateImgDir = DefaultRepositoryDir.getOrgTemplateImgDir(DEFAULT_ORGANIZATION_ID);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(orgTemplateImgDir + "/" + fileId);
        fileRequest.setFileName(fileName);
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
    }
}
