package io.metersphere.ai.engine.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件读取工具类，支持本地文件、ClassPath资源、InputStream
 */
public class FileReaderTool {
    /**
     * 读取文件内容（自动识别路径类型）
     *
     * @param filePath 文件路径（支持绝对路径、相对路径、classpath:前缀）
     * @return 提取的文本内容
     */
    @Tool(description = "根据文件地址读取文件内容")
    public String readFile(String filePath) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(
                    new FileSystemResource(filePath)
            );

            // 提取文本
            var documents = reader.get();
            return documents.isEmpty() ? "" : documents.getFirst().getFormattedContent();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 读取文件并返回 Document 列表
     */
    public List<Document> readFileAsDocuments(String filePath) throws IOException {
        Resource resource = resolveResource(filePath);
        return new TikaDocumentReader(resource).get();
    }

    /**
     * 从 InputStream 读取文件
     */
    public String readFromStream(InputStream inputStream) {
        TikaDocumentReader reader = new TikaDocumentReader(convertToResource(inputStream));
        List<Document> documents = reader.get();
        return documents.isEmpty() ? "" : documents.getFirst().getFormattedContent();
    }

    /**
     * 解析资源路径（自动判断是否为 classpath 或文件系统）
     */
    private Resource resolveResource(String filePath) {
        if (filePath.startsWith("classpath:")) {
            return new ClassPathResource(filePath.substring("classpath:".length()));
        } else {
            return new FileSystemResource(filePath);
        }
    }

    /**
     * 读取 ClassPath 下的文件
     */
    public String readClassPathFile(String classPath) throws IOException {
        try (InputStream inputStream = new ClassPathResource(classPath).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private static Resource convertToResource(InputStream inputStream) {
        // 直接包装 InputStream
        return new InputStreamResource(inputStream);
    }
}