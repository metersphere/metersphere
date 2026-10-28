package io.metersphere.sdk.file;

import io.metersphere.sdk.constants.LocalRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LocalFileRepositoryTest {
    private final LocalFileRepository localFileRepository = new LocalFileRepository();

    @BeforeAll
    public static void initTranslator() throws Exception {
        // 单元测试不启动 Spring 容器，初始化 Translator 避免获取异常信息时出现空指针
        Field field = Translator.class.getDeclaredField("messageSource");
        field.setAccessible(true);
        field.set(null, new StaticMessageSource());
    }

    @Test
    public void saveAndReadFile() throws Exception {
        String folder = LocalRepositoryDir.getRootDir() + "/local-file-repository-test-" + UUID.randomUUID();
        String fileName = "test.txt";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        FileRequest fileRequest = new FileRequest(folder, StorageType.LOCAL.name(), fileName);

        String savedPath = localFileRepository.saveFile(content, fileRequest);
        Path expectedPath = Paths.get(folder).toAbsolutePath().normalize().resolve(fileName);
        Assertions.assertEquals(expectedPath.toString(), Paths.get(savedPath).toAbsolutePath().normalize().toString());
        Assertions.assertArrayEquals(content, localFileRepository.getFile(fileRequest));
        Assertions.assertEquals(content.length, localFileRepository.getFileSize(fileRequest));
        try (InputStream inputStream = localFileRepository.getFileAsStream(fileRequest)) {
            Assertions.assertArrayEquals(content, inputStream.readAllBytes());
        }

        // fileName 为空时删除整个目录
        localFileRepository.delete(new FileRequest(folder, StorageType.LOCAL.name(), null));
        Assertions.assertFalse(Files.exists(expectedPath.getParent()));
    }

    @Test
    public void rejectFolderOutsideRoot() {
        // 根目录的上级目录，不在允许访问的范围内
        String outsideFolder = Paths.get(LocalRepositoryDir.getRootDir()).toAbsolutePath().normalize().getParent().toString();
        Assertions.assertThrows(MSException.class, () -> localFileRepository.getFileAsStream(
                new FileRequest(outsideFolder, StorageType.LOCAL.name(), "metersphere")));
        Assertions.assertThrows(MSException.class, () -> localFileRepository.saveFile(
                "content".getBytes(StandardCharsets.UTF_8),
                new FileRequest(outsideFolder, StorageType.LOCAL.name(), "test.txt")));
        Assertions.assertThrows(MSException.class, () -> localFileRepository.delete(
                new FileRequest(outsideFolder, StorageType.LOCAL.name(), "metersphere")));
    }

    @Test
    public void rejectFileNameEscapeFolder() {
        String folder = LocalRepositoryDir.getRootDir() + "/local-file-repository-test-" + UUID.randomUUID();
        // 文件名不能跳出所在目录
        Assertions.assertThrows(MSException.class, () -> localFileRepository.getFile(
                new FileRequest(folder, StorageType.LOCAL.name(), "..")));
        Assertions.assertThrows(MSException.class, () -> localFileRepository.saveFile(
                "content".getBytes(StandardCharsets.UTF_8),
                new FileRequest(folder, StorageType.LOCAL.name(), "../escape.txt")));
    }
}
