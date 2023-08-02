package io.metersphere.sdk.plugin.storage;

import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;

/**
 * jar包静态资源存储策略
 * @author jianxing
 */
public class MsStorageStrategy implements StorageStrategy {


    private final FileRepository fileRepository;
    private final String pluginId;

    public static final String DIR_PATH = "system/plugin";

    public MsStorageStrategy(String pluginId) {
        this.pluginId = pluginId;
        fileRepository = FileCenter.getDefaultRepository();
    }

    @Override
    public String store(String name, InputStream in) throws Exception {
        FileRequest request = getFileRequest(name);
            return fileRepository.saveFile(in.readAllBytes(), request);
    }

    @Override
    public InputStream get(String name) throws Exception {
        FileRequest request = getFileRequest(name);
        return fileRepository.getFileAsStream(request);
    }

    @Override
    public List<String> getFolderFileNames(String dirName) throws Exception {
        FileRequest request = getFileRequest(dirName);
        List<String> fileNames = fileRepository.getFolderFileNames(request);
        return fileNames.stream().map(s -> s.replace(getPluginDir(), StringUtils.EMPTY)).toList();
    }

    @Override
    public void delete() throws Exception {
        FileRequest request = new FileRequest();
        request.setProjectId(getPluginDir());
        fileRepository.deleteFolder(request);
    }

    private FileRequest getFileRequest(String name) {
        FileRequest request = new FileRequest();
        request.setProjectId(getPluginDir());
        request.setFileName(name);
        return request;
    }

    private String getPluginDir() {
        return DIR_PATH + "/" + this.pluginId;
    }
}
