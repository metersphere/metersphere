package io.metersphere.system.controller;

import io.metersphere.system.file.FileCenter;
import io.metersphere.system.file.FileRepository;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
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
        FileRequest request = new FileRequest();
        request.setFileName("test.txt");
        request.setProjectId("test-project");
        request.setResourceId("test-resource-id");
        repository.saveFile(mockFile, request);
        Assertions.assertTrue(repository.saveFile(mockFile, request) != null);
        Assertions.assertTrue(repository.saveFile("Hello, World!".getBytes(), request) != null);

    }

    @Test
    @Order(3)
    public void testGetFile() throws Exception {
        // 创建一个FileRequest对象作为测试用的请求参数
        FileRequest request = new FileRequest();
        request.setFileName("test.txt");
        request.setProjectId("test-project");
        request.setResourceId("test-resource-id");
        repository.getFile(request);
        Assertions.assertTrue(repository.getFile(request) != null);
    }

    @Test
    @Order(4)
    public void testDelFile() throws Exception {
        // 创建一个FileRequest对象作为测试用的请求参数
        FileRequest request = new FileRequest();
        request.setFileName("test.txt");
        request.setProjectId("test-project");
        request.setResourceId("test-resource-id");
        repository.delete(request);
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
        request.setProjectId("test-project");
        repository.saveFile(mockFile, request);
        Assertions.assertTrue(repository.saveFile(mockFile, request) != null);
        Assertions.assertTrue(repository.saveFile("Hello, World!".getBytes(), request) != null);

    }
}
