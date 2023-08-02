package io.metersphere.sdk.plugin.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * jar包、图片、前端配置文件等静态资源存储策略
 * @author jianxing
 */
public interface StorageStrategy {

    /**
     * 存储文件
     * @param name
     * @param in
     * @return
     * @throws IOException
     */
    String store(String name, InputStream in) throws Exception;

    /**
     * 获取文件
     * @param path
     * @return
     * @throws IOException
     */
    InputStream get(String path) throws Exception;

    /**
     * 获取指定文件夹下的文件名列表
     *
     * @param dirName
     * @throws Exception
     */
    List<String> getFolderFileNames(String dirName) throws Exception;

    /**
     * 删除文件
     * @throws IOException
     */
    void delete() throws Exception;
}
