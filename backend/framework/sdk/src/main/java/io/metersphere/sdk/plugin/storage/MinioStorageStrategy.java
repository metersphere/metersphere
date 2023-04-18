package io.metersphere.sdk.plugin.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * jar包静态资源存储策略，存储在 Minio
 */
public class MinioStorageStrategy implements StorageStrategy {

    private String pluginId;

    public static final String DIR_PATH = "system/plugin";

    public MinioStorageStrategy(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String store(String name, InputStream in) throws IOException {
        // todo 上传到 minio
        return null;
    }

    @Override
    public InputStream get(String name) {
        // todo 获取文件
        return null;
    }

    @Override
    public void delete() throws IOException {
        // todo 删除文件
    }
}
