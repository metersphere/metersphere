package io.metersphere.metadata.repository;

import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.MinioProperties;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.vo.FileRequest;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinIOFileRepository implements FileRepository {

    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioProperties minioProperties;

    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws Exception {
        FileUtils.validateFileName(request.getFileName());
        String bucket = minioProperties.getBucket();
        String fileName = request.getProjectId() + "/" + request.getFileName();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket) // 存储桶
                .object(fileName) // 文件名
                .stream(file.getInputStream(), file.getSize(), -1) // 文件内容
                .build());
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucket, fileName);
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws Exception {
        FileUtils.validateFileName(request.getFileName());
        String bucket = minioProperties.getBucket();
        String fileName = request.getProjectId() + "/" + request.getFileName();
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)
        ) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket) // 存储桶
                    .object(fileName) // 文件名
                    .stream(inputStream, bytes.length, -1) // 文件内容
                    //                .contentType(file.getContentType()) // 文件类型
                    .build());
        }
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucket, fileName);
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        String bucket = minioProperties.getBucket();
        String fileName = request.getProjectId();
        if (StringUtils.isNotBlank(request.getFileName())) {
            fileName += "/" + request.getFileName();
        }
        if (fileName.endsWith("/")) {
            // 删除文件夹
            removeObjects(bucket, fileName);
        } else {
            // 删除单个文件
            removeObject(bucket, fileName);
        }
    }


    private boolean removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName) // 存储桶
                .object(objectName) // 文件名
                .build());
        return true;
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
        Iterable<Result<Item>> results = minioClient.listObjects(
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
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        String bucket = minioProperties.getBucket();
        String fileName = request.getProjectId() + "/" + request.getFileName();

        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket) // 存储桶
                .object(fileName) // 文件名
                .build());
    }

    @Override
    public List<FileInfoDTO> getFileBatch(List<FileRequest> requestList) throws Exception {
        List<FileInfoDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(requestList)) {
            for (FileRequest fileRequest : requestList) {
                FileInfoDTO fileInfoDTO = new FileInfoDTO(fileRequest.getResourceId(), fileRequest.getFileName(), fileRequest.getType(), fileRequest.getProjectId(), fileRequest.getUpdateTime(), fileRequest.getStorage(), fileRequest.getPath(), this.getFile(fileRequest));
                list.add(fileInfoDTO);
            }
        }
        return list;
    }


    @Override
    public boolean reName(FileRequest request) throws Exception {
        String bucket = minioProperties.getBucket();
        String beforeName = request.getProjectId() + "/" + request.getBeforeName();
        String fileName = request.getProjectId() + "/" + request.getFileName();
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .source(CopySource.builder().bucket(bucket).object(beforeName).build())
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }

        // 删除旧文件
        request.setFileName(request.getBeforeName());
        delete(request);
        return true;
    }
}
