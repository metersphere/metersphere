package io.metersphere.system.controller;

import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileCenterTests {

    @Resource
    private MinioRepository repository;

    @Test
    @Order(1)
    public void testRepository() throws Exception {
        FileRepository repository = FileCenter.getDefaultRepository();
        Assertions.assertTrue(repository instanceof MinioRepository);
    }

    @Test
    @Order(2)
    public void testSaveFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );
        // 创建一个FileRequest对象作为测试用的请求参数
        FileRequest request = getFileRequest();
        repository.saveFile(mockFile, request);
        Assertions.assertTrue(repository.saveFile(mockFile, request) != null);
        Assertions.assertTrue(repository.saveFile("Hello, World!".getBytes(), request) != null);

    }

    private static FileRequest getFileRequest() {
        FileRequest request = new FileRequest();
        request.setFileName("test.txt");
        request.setFolder("system/test-project/test-resource-id");
        return request;
    }

    @Test
    @Order(3)
    public void testGetFile() throws Exception {
        // 创建一个FileRequest对象作为测试用的请求参数
        FileRequest request = getFileRequest();
        repository.getFile(request);
        Assertions.assertTrue(repository.getFile(request) != null);
    }

    @Test
    @Order(4)
    public void testDelFile() throws Exception {
        // 创建一个FileRequest对象作为测试用的请求参数
        repository.delete(getFileRequest());
    }

    @Test
    @Order(5)
    public void testFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes()
        );
        // 创建一个FileRequest对象作为测试用的请求参数
        FileRequest request = new FileRequest();
        request.setFileName("test.txt");
        request.setFolder("system/test-project");
        repository.saveFile(mockFile, request);
        Assertions.assertTrue(repository.saveFile(mockFile, request) != null);
        Assertions.assertTrue(repository.saveFile("Hello, World!".getBytes(), request) != null);

    }
}
