package io.metersphere.commons.utils;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.dto.AttachmentBodyFile;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.enums.JmxFileMetadataColumns;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.request.BodyFile;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.TemporaryFileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApiFileUtil extends FileUtils {
    private static FileManagerService fileManagerService;
    private static FileMetadataService fileMetadataService;
    private static TemporaryFileUtil temporaryFileUtil;

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
        if (CollectionUtils.isNotEmpty(bodyFiles) && StringUtils.isNotBlank(requestId)) {
            for (MultipartFile bodyFile : bodyFiles) {
                fileManagerService.upload(bodyFile, getRequest(requestId));
            }
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
        LoggerUtil.info("开始从MinIO处理文件：", path);
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

    public static List<AttachmentBodyFile> getExecuteFile(HashTree tree, String reportId, boolean isLocal) {
        if (temporaryFileUtil == null) {
            temporaryFileUtil = CommonBeanFactory.getBean(TemporaryFileUtil.class);
        }
        List<AttachmentBodyFile> fileList = new ArrayList<>();
        formatFilePathForNode(tree, reportId, isLocal, fileList);
        return fileList;
    }

    private static void formatFilePathForNode(HashTree tree, String reportId, boolean isLocal, List<AttachmentBodyFile> fileList) {
        if (tree != null) {
            if (fileMetadataService == null) {
                fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
            }

            for (Object key : tree.keySet()) {
                if (key == null) {
                    continue;
                }
                HashTree node = tree.get(key);
                if (key instanceof HTTPSamplerProxy) {
                    getAttachmentBodyFileByHttp(key, reportId, isLocal, fileList);
                } else if (key instanceof CSVDataSet) {
                    getAttachmentBodyFileByCsv(key, reportId, isLocal, fileList);
                }
                if (node != null) {
                    formatFilePathForNode(node, reportId, isLocal, fileList);
                }
            }
        }
    }

    public static void getAttachmentBodyFileByCsv(Object tree, String reportId, boolean isLocal, List<AttachmentBodyFile> bodyFileList) {
        CSVDataSet source = (CSVDataSet) tree;
        if (StringUtils.isNotEmpty(source.getPropertyAsString(ElementConstants.FILENAME))) {
            getAttachmentFileByTestElement(source, reportId, isLocal, bodyFileList);
        }
    }

    public static void getAttachmentBodyFileByHttp(Object testElement, String reportId, boolean isLocal, List<AttachmentBodyFile> fileList) {
        if (testElement == null) {
            return;
        }
        HTTPSamplerProxy source = (HTTPSamplerProxy) testElement;
        for (HTTPFileArg httpFileArg : source.getHTTPFiles()) {
            getAttachmentFileByTestElement(httpFileArg, reportId, isLocal, fileList);
        }
    }

    private static void getAttachmentFileByTestElement(TestElement testElement, String reportId, boolean isLocal, List<AttachmentBodyFile> bodyFileList) {
        if (testElement == null) {
            return;
        }
        String defaultFileName = null;
        if (testElement instanceof HTTPFileArg) {
            defaultFileName = ((HTTPFileArg) testElement).getPath();
        } else {
            defaultFileName = testElement.getPropertyAsString(ElementConstants.FILENAME);
        }
        if (testElement.getPropertyAsBoolean(ElementConstants.IS_REF)) {
            FileMetadataWithBLOBs fileMetadata = fileMetadataService.getFileMetadataById(
                    testElement.getPropertyAsString(ElementConstants.FILE_ID));
            if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {

                String path = getFilePathInJxm(reportId, fileMetadata.getName());

                AttachmentBodyFile attachmentBodyFile = new AttachmentBodyFile();
                attachmentBodyFile.setFileMetadataId(fileMetadata.getId());
                attachmentBodyFile.setFileStorage(fileMetadata.getStorage());
                attachmentBodyFile.setName(fileMetadata.getName());
                attachmentBodyFile.setFileUpdateTime(fileMetadata.getUpdateTime());
                attachmentBodyFile.setProjectId(fileMetadata.getProjectId());
                attachmentBodyFile.setFilePath(path);
                if (StringUtils.isNotBlank(fileMetadata.getAttachInfo())) {
                    attachmentBodyFile.setFileAttachInfoJson(fileMetadata.getAttachInfo());
                }
                bodyFileList.add(attachmentBodyFile);

                if (!isLocal) {
                    testElement.setProperty(JmxFileMetadataColumns.REF_FILE_STORAGE.name(), fileMetadata.getStorage());
                    testElement.setProperty(JmxFileMetadataColumns.REF_FILE_NAME.name(), fileMetadata.getName());
                    testElement.setProperty(JmxFileMetadataColumns.REF_FILE_UPDATE_TIME.name(), fileMetadata.getUpdateTime());
                    testElement.setProperty(JmxFileMetadataColumns.REF_FILE_PROJECT_ID.name(), fileMetadata.getProjectId());
                    if (StringUtils.isNotBlank(fileMetadata.getAttachInfo())) {
                        testElement.setProperty(JmxFileMetadataColumns.REF_FILE_ATTACH_INFO.name(), fileMetadata.getAttachInfo());
                    }
                } else {
                    path = temporaryFileUtil.generateFilePath(attachmentBodyFile.getProjectId(), attachmentBodyFile.getFileUpdateTime(), attachmentBodyFile.getName());
                }

                testElement.setProperty(ElementConstants.FILENAME, path);
                if (testElement instanceof HTTPFileArg) {
                    ((HTTPFileArg) testElement).setPath(path);
                }

            }
        } else {
            if (StringUtils.isNotBlank(defaultFileName) && new File(defaultFileName).exists()) {
                //判断本地文件
                AttachmentBodyFile attachmentBodyFile = new AttachmentBodyFile();
                attachmentBodyFile.setFileStorage(StorageConstants.LOCAL.name());
                attachmentBodyFile.setName(defaultFileName);
                attachmentBodyFile.setFilePath(defaultFileName);
                bodyFileList.add(attachmentBodyFile);
            } else if (StringUtils.isNotBlank(testElement.getPropertyAsString(ElementConstants.RESOURCE_ID))) {
                // 从MinIO下载
                AttachmentBodyFile attachmentBodyFile = new AttachmentBodyFile();
                attachmentBodyFile.setFileStorage(StorageConstants.MINIO.name());
                attachmentBodyFile.setName(testElement.getPropertyAsString(ElementConstants.RESOURCE_ID));
                attachmentBodyFile.setFilePath(defaultFileName);
                bodyFileList.add(attachmentBodyFile);
            }
        }
    }


    private static String getFilePathInJxm(String reportId, String fileName) {
        return StringUtils.join(BODY_FILE_DIR, File.separator, reportId, File.separator, fileName);
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
            if (key == null) {
                continue;
            }
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
        if (StringUtils.isNotEmpty(source.getPropertyAsString(ElementConstants.FILENAME))) {
            BodyFile file = new BodyFile();
            file.setId(source.getPropertyAsString(ElementConstants.FILENAME));
            file.setName(source.getPropertyAsString(ElementConstants.FILENAME));

            if (source.getPropertyAsBoolean(ElementConstants.IS_REF)) {
                FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(
                        source.getPropertyAsString(ElementConstants.FILE_ID));
                if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                    file.setStorage(fileMetadata.getStorage());
                    file.setFileId(source.getPropertyAsString(ElementConstants.FILE_ID));
                    String fileName = StringUtils.join(reportId, File.separator, fileMetadata.getName());
                    file.setName(fileName);
                    String path = getFilePathInJxm(reportId, fileMetadata.getName());
                    ((CSVDataSet) key).setProperty(ElementConstants.FILENAME, path);
                }
            } else if (!new File(source.getPropertyAsString(ElementConstants.FILENAME)).exists()
                    && StringUtils.isNotBlank(source.getPropertyAsString(ElementConstants.RESOURCE_ID))) {
                // 从MinIO下载
                downloadFile(source.getPropertyAsString(ElementConstants.RESOURCE_ID),
                        source.getPropertyAsString(ElementConstants.FILENAME));
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
                FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(
                        httpFileArg.getPropertyAsString(ElementConstants.FILE_ID));

                if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                    file.setStorage(fileMetadata.getStorage());
                    file.setFileId(httpFileArg.getPropertyAsString(ElementConstants.FILE_ID));
                    file.setName(reportId + File.separator + fileMetadata.getName());
                    String path = getFilePathInJxm(reportId, fileMetadata.getName());
                    httpFileArg.setPath(path);
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
