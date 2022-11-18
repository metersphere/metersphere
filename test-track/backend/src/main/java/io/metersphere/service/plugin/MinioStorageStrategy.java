package io.metersphere.service.plugin;

import im.metersphere.plugin.storage.StorageStrategy;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * jar包静态资源存储策略，存储在 Minio
 */
public class MinioStorageStrategy implements StorageStrategy {

    private FileManagerService fileManagerService;
    private String pluginId;

    public static final String DIR_PATH = "system/plugin";

    public MinioStorageStrategy(String pluginId) {
        this.pluginId = pluginId;
        fileManagerService = CommonBeanFactory.getBean(FileManagerService.class);
    }

    @Override
    public String store(String name, InputStream in) throws IOException {
        FileRequest request = getFileRequest(name);
        return fileManagerService.upload(in.readAllBytes(), request);
    }

    @Override
    public InputStream get(String name) {
        FileRequest request = getFileRequest(name);
        return fileManagerService.downloadFileAsStream(request);
    }

    @Override
    public void delete() throws IOException {
        FileRequest request = new FileRequest();
        request.setProjectId(DIR_PATH + "/" + this.pluginId + "/");
        request.setStorage(StorageConstants.MINIO.name());
        fileManagerService.delete(request);
    }

    private FileRequest getFileRequest(String name) {
        FileRequest request = new FileRequest();
        request.setProjectId(DIR_PATH + "/" + this.pluginId);
        request.setFileName(name);
        request.setStorage(StorageConstants.MINIO.name());
        return request;
    }
}
