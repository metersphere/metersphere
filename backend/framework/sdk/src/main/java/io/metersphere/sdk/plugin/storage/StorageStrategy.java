package io.metersphere.sdk.plugin.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * jar包、图片、前端配置文件等静态资源存储策略
 */
public interface StorageStrategy {

    /**
     * 存储文件
     * @param name
     * @param in
     * @return
     * @throws IOException
     */
    String store(String name, InputStream in) throws IOException;

    /**
     * 获取文件
     * @param path
     * @return
     * @throws IOException
     */
    InputStream get(String path) throws IOException;


    /**
     * 删除文件
     * @throws IOException
     */
    void delete() throws IOException;
}
