package io.metersphere.sdk.file;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.minio.*;
import io.minio.messages.Item;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MinioRepository implements FileRepository {

    private MinioClient client;
    // 缓冲区大小
    private static final int BUFFER_SIZE = 8192;
    public static final String BUCKET = "metersphere";
    public static final String ENDPOINT = "endpoint";
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";

    /**
     * 初始化
     */
    public void init(MinioClient client) {
        if (this.client == null) {
            this.client = client;
        }
    }

    /**
     * 根据配置信息动态创建
     */
    public void init(Map<String, Object> minioConfig) {
        if (minioConfig == null || minioConfig.isEmpty()) {
            LogUtils.info("MinIO初始化失败，参数[minioConfig]为空");
            return;
        }

        try {
            Object serverUrl = minioConfig.get(ENDPOINT).toString();
            if (ObjectUtils.isNotEmpty(serverUrl)) {
                // 创建 MinioClient 客户端
                client = MinioClient.builder()
                        .endpoint(minioConfig.get(ENDPOINT).toString())
                        .credentials(minioConfig.get(ACCESS_KEY).toString(), minioConfig.get(SECRET_KEY).toString())
                        .build();
                boolean exist = client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
                if (!exist) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
                }
            }
        } catch (Exception e) {
            LogUtils.error("MinIOClient初始化失败！", e);
        }
    }


    private String getPath(FileRequest request) {
        String folder = request.getFolder();
        if (!StringUtils.startsWithAny(folder, "system", "project", "organization")) {
            throw new MSException("folder.error");
        }
        return StringUtils.join(folder, "/", request.getFileName());
    }

    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws Exception {
        // 文件存储路径
        String filePath = getPath(request);
        client.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
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
                    .bucket(BUCKET)
                    .object(filePath)
                    .stream(inputStream, bytes.length, -1)
                    .build());
        }
        return request.getFileName();
    }

    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        String filePath = getPath(request);
        client.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(filePath)
                .stream(inputStream, -1, 5242880) // 文件内容
                .build());
        return filePath;
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        String filePath = getPath(request);
        // 删除单个文件
        removeObject(BUCKET, filePath);
    }

    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        String filePath = getPath(request);
        // 删除文件夹
        removeObjects(BUCKET, filePath);
    }

    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return listObjects(BUCKET, getPath(request));
    }

    @Override
    public void copyFile(FileCopyRequest request) throws Exception {
        String sourcePath = StringUtils.join(request.getCopyFolder(), "/", request.getCopyfileName());
        String targetPath = getPath(request);
        client.copyObject(CopyObjectArgs.builder()
                .bucket(BUCKET)
                .object(targetPath)
                .source(CopySource.builder()
                        .bucket(BUCKET)
                        .object(sourcePath)
                        .build())
                .build());
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
                        .bucket(BUCKET)
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
                .bucket(BUCKET) // 存储桶
                .object(fileName) // 文件名
                .build());
    }


    @Override
    public long getFileSize(FileRequest request) throws Exception {
        String fileName = getPath(request);
        return client.statObject(StatObjectArgs.builder()
                .bucket(BUCKET) // 存储桶
                .object(fileName) // 文件名
                .build()).size();
    }
}
