package io.metersphere.sdk.file;

import io.metersphere.sdk.config.MinioConfig;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinioRepository implements FileRepository {

    @Resource
    private MinioClient client;
    // 缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    private String getPath(FileRequest request) {
        // 文件存储路径
        return StringUtils.join(
                request.getProjectId(),
                "/",
                StringUtils.isNotBlank(request.getResourceId()) ? request.getResourceId() + "/" : StringUtils.EMPTY,
                request.getFileName());
    }

    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws Exception {
        // 文件存储路径
        String filePath = getPath(request);
        client.putObject(PutObjectArgs.builder()
                .bucket(MinioConfig.BUCKET)
                .object(filePath)
                .stream(file.getInputStream(), file.getSize(), -1) // 文件内容
                .build());
        return filePath;
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws Exception {
        String filePath = getPath(request);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(MinioConfig.BUCKET)
                    .object(filePath)
                    .stream(inputStream, bytes.length, -1)
                    .build());
        }
        return request.getFileName();
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        String filePath = getPath(request);
        // 删除单个文件
        removeObject(MinioConfig.BUCKET, filePath);
    }

    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        String filePath = getPath(request);
        // 删除文件夹
        removeObjects(MinioConfig.BUCKET, filePath);
    }

    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return listObjects(MinioConfig.BUCKET, getPath(request));
    }

    private void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName) // 存储桶
                .object(objectName) // 文件名
                .build());
    }

    public void removeObjects(String bucketName, String objectName) throws Exception {
        List<String> objects = listObjects(bucketName, objectName);
        for (String object : objects) {
            removeObject(bucketName, object);
        }
    }

    /**
     * 递归获取某路径下的所有文件
     */
    public List<String> listObjects(String bucketName, String objectName) throws Exception {
        List<String> list = new ArrayList<>(12);
        Iterable<Result<Item>> results = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(objectName)
                        .build());
        for (Result<Item> result : results) {
            Item item = result.get();
            if (item.isDir()) {
                List<String> files = listObjects(bucketName, item.objectName());
                list.addAll(files);
            } else {
                list.add(item.objectName());
            }
        }
        return list;
    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        return getFileAsStream(request).readAllBytes();
    }

    @Override
    public void downloadFile(FileRequest request, String fullPath) throws Exception {
        String fileName = getPath(request);
        // 下载对象到本地文件
        try (InputStream inputStream = client.getObject(
                GetObjectArgs.builder()
                        .bucket(MinioConfig.BUCKET)
                        .object(fileName)
                        .build());
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullPath))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        String fileName = getPath(request);
        return client.getObject(GetObjectArgs.builder()
                .bucket(MinioConfig.BUCKET) // 存储桶
                .object(fileName) // 文件名
                .build());
    }
}
