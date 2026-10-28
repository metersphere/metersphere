package io.metersphere.sdk.file;

import io.metersphere.sdk.constants.LocalRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class LocalFileRepository implements FileRepository {
    @Override
    public String saveFile(MultipartFile multipartFile, FileRequest request) throws IOException {
        if (multipartFile == null || request == null || StringUtils.isEmpty(request.getFileName()) || StringUtils.isEmpty(request.getFolder())) {
            return null;
        }
        Path filePath = getFilePath(request);
        createFileDir(filePath.getParent());
        File file = filePath.toFile();
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        return file.getPath();
    }

    private void createFileDir(Path dir) {
        if (dir == null) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        File fileDir = dir.toFile();
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + fileDir);
        }
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws IOException {
        Path filePath = getFilePath(request);
        createFileDir(filePath.getParent());
        File file = filePath.toFile();
        try (OutputStream ops = new FileOutputStream(file)) {
            ops.write(bytes);
            return file.getPath();
        }
    }

    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        Path filePath = getFilePath(request);
        createFileDir(filePath.getParent());
        File file = filePath.toFile();
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file.getPath();
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        Path path;
        if (StringUtils.isBlank(request.getFileName())) {
            // 删除目录
            path = getFolderPath(request);
        } else {
            path = getFilePath(request);
        }
        File file = path.toFile();
        FileUtil.deleteContents(file);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete file: " + path);
        }
    }

    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        MsFileUtils.validateFileName(request.getFolder(), request.getFileName());
        this.delete(request);
    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        File file = getFilePath(request).toFile();
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return new FileInputStream(getFilePath(request).toFile());
    }

    @Override
    public void downloadFile(FileRequest request, String localPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getFolderFileNames(FileRequest request) {
        return null;
    }

    @Override
    public void copyFile(FileCopyRequest request) throws Exception {
        throw new MSException("Not support copy file");
    }

    @Override
    public long getFileSize(FileRequest request) throws Exception {
        File file = getFilePath(request).toFile();
        return file.length();
    }

    /**
     * 获取文件路径，并校验文件必须在根目录下
     */
    private Path getFilePath(FileRequest request) {
        if (request == null || StringUtils.isBlank(request.getFileName())) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        Path folderPath = getFolderPath(request);
        Path filePath = folderPath.resolve(request.getFileName()).normalize();
        if (!filePath.startsWith(folderPath)) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        return filePath;
    }

    /**
     * 获取目录路径
     * 相对路径基于根目录解析，绝对路径必须在根目录下
     */
    private Path getFolderPath(FileRequest request) {
        if (request == null || StringUtils.isBlank(request.getFolder())) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        MsFileUtils.validateFileName(request.getFolder(), request.getFileName());
        Path rootPath = getRootPath();
        Path folderPath = Paths.get(request.getFolder());
        if (!folderPath.isAbsolute()) {
            folderPath = rootPath.resolve(folderPath);
        }
        folderPath = folderPath.normalize();
        if (!folderPath.startsWith(rootPath)) {
            throw new MSException(Translator.get("invalid_parameter"));
        }
        return folderPath;
    }

    private Path getRootPath() {
        return Paths.get(LocalRepositoryDir.getRootDir()).toAbsolutePath().normalize();
    }
}
