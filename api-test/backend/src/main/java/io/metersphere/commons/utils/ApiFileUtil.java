package io.metersphere.commons.utils;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.request.BodyFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jorphan.collections.HashTree;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public class ApiFileUtil extends FileUtils {
    private static FileManagerService fileManagerService;
    private static FileMetadataService fileMetadataService;

    public static String getFilePath(BodyFile file) {
        String type = StringUtils.isNotEmpty(file.getFileType()) ? file.getFileType().toLowerCase() : null;
        String name = file.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtil.BODY_FILE_DIR, File.separator, file.getProjectId(), File.separator, name);
    }

    public static void createFiles(List<FileInfoDTO> infoDTOS) {
        infoDTOS.forEach(item -> {
            createFile(item.getPath(), item.getFileByte());
        });
    }

    public static void copyBodyFiles(String sourceId, String targetId) {
        // 本地存储
        FileUtils.copyBodyFiles(sourceId, targetId);
    }

    public static void createBodyFiles(String requestId, List<MultipartFile> bodyFiles) {
        FileUtils.createBodyFiles(requestId, bodyFiles);
        // MinIO存储
        if (fileManagerService == null) {
            fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
        }
        for (MultipartFile bodyFile : bodyFiles) {
            fileManagerService.upload(bodyFile, getRequest(requestId));
        }
    }

    public static void createBodyFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyUploadIds) && CollectionUtils.isNotEmpty(bodyFiles)
                && bodyUploadIds.size() == bodyFiles.size()) {
            FileUtils.createBodyFiles(bodyUploadIds, bodyFiles);
            // MinIO存储
            if (fileManagerService == null) {
                fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
            }
            for (int i = 0; i < bodyUploadIds.size(); i++) {
                MultipartFile bodyFile = bodyFiles.get(i);
                fileManagerService.upload(bodyFile, getRequest(bodyUploadIds.get(i)));
            }
        }
    }

    public static void deleteBodyFiles(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return;
        }
        FileUtils.deleteBodyFiles(requestId);
        // MinIO文件删除
        if (fileManagerService == null) {
            fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
        }
        fileManagerService.delete(getRequest(requestId));
    }

    private static FileRequest getRequest(String requestId) {
        FileRequest request = new FileRequest();
        String path = StringUtils.join(BODY_FILE_DIR, File.separator, requestId);
        request.setProjectId(path);
        request.setFileName(requestId);
        request.setStorage(StorageConstants.MINIO.name());
        return request;
    }

    public static void downloadFile(String requestId, String path) {
        // MinIO文件下载
        if (fileManagerService == null) {
            fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
        }
        byte[] content = fileManagerService.downloadFile(getRequest(requestId));
        if (ArrayUtils.isNotEmpty(content)) {
            FileUtils.createFile(path, content);
        }
    }

    /**
     * 获取当前jmx 涉及到的文件  执行时
     *
     * @param tree
     */
    public static void getExecuteFiles(HashTree tree, String reportId, List<BodyFile> files) {
        if (fileMetadataService == null) {
            fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        }
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof HTTPSamplerProxy) {
                dealWithHttp(key, reportId, files);
            } else if (key instanceof CSVDataSet) {
                dealWithCsv(key, reportId, files);
            }
            if (node != null) {
                getExecuteFiles(node, reportId, files);
            }
        }
    }

    private static void dealWithCsv(Object key, String reportId, List<BodyFile> files) {
        CSVDataSet source = (CSVDataSet) key;
        if (source != null && StringUtils.isNotEmpty(source.getPropertyAsString(ElementConstants.FILENAME))) {
            BodyFile file = new BodyFile();
            file.setId(source.getPropertyAsString(ElementConstants.FILENAME));
            file.setName(source.getPropertyAsString(ElementConstants.FILENAME));
            if (source.getPropertyAsBoolean(ElementConstants.IS_REF)) {
                FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(source.getPropertyAsString(ElementConstants.FILE_ID));
                if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                    file.setStorage(fileMetadata.getStorage());
                    file.setFileId(source.getPropertyAsString(ElementConstants.FILE_ID));
                    file.setName(reportId + File.separator + fileMetadata.getName());
                    ((CSVDataSet) key).setProperty(ElementConstants.FILENAME, BODY_FILE_DIR + File.separator + reportId + File.separator + fileMetadata.getName());
                }
            } else if (!new File(source.getPropertyAsString(ElementConstants.FILENAME)).exists()
                    && StringUtils.isNotBlank(source.getPropertyAsString(ElementConstants.RESOURCE_ID))) {
                // 从MinIO下载
                downloadFile(source.getPropertyAsString(ElementConstants.RESOURCE_ID), source.getPropertyAsString(ElementConstants.FILENAME));
            }
            files.add(file);
        }
    }

    private static void dealWithHttp(Object key, String reportId, List<BodyFile> files) {
        HTTPSamplerProxy source = (HTTPSamplerProxy) key;
        if (source == null || source.getHTTPFiles().length == 0) {
            return;
        }
        for (HTTPFileArg httpFileArg : source.getHTTPFiles()) {
            BodyFile file = new BodyFile();
            file.setId(httpFileArg.getParamName());
            file.setName(httpFileArg.getPath());
            if (httpFileArg.getPropertyAsBoolean(ElementConstants.IS_REF)) {
                FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(httpFileArg.getPropertyAsString(ElementConstants.FILE_ID));
                if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                    file.setStorage(fileMetadata.getStorage());
                    file.setFileId(httpFileArg.getPropertyAsString(ElementConstants.FILE_ID));
                    file.setName(reportId + File.separator + fileMetadata.getName());
                    httpFileArg.setPath(BODY_FILE_DIR + File.separator + reportId + File.separator + fileMetadata.getName());
                }
            } else if (!new File(httpFileArg.getPath()).exists()
                    && StringUtils.isNotBlank(httpFileArg.getPropertyAsString(ElementConstants.RESOURCE_ID))) {
                // 从MinIO下载
                downloadFile(httpFileArg.getPropertyAsString(ElementConstants.RESOURCE_ID), httpFileArg.getPath());
            }
            files.add(file);
        }
    }
}
