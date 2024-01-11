package io.metersphere.sdk.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileRepository {
    /**
     * 保存文件
     *
     * @param file
     * @param request
     * @return
     * @throws Exception
     */
    String saveFile(MultipartFile file, FileRequest request) throws Exception;

    /**
     * 保存文件
     *
     * @param bytes
     * @param request
     * @return
     * @throws Exception
     */
    String saveFile(byte[] bytes, FileRequest request) throws Exception;

    /**
     * 保存文件
     *
     * @param inputStream
     * @param request
     * @return
     * @throws Exception
     */
    String saveFile(InputStream inputStream, FileRequest request) throws Exception;

    /**
     * 删除文件
     *
     * @param request
     * @throws Exception
     */
    void delete(FileRequest request) throws Exception;

    /**
     * 删除文件夹
     *
     * @param request
     * @throws Exception
     */
    void deleteFolder(FileRequest request) throws Exception;


    /**
     * 获取文件字节内容，大文件不建议使用
     *
     * @param request
     * @return
     * @throws Exception
     */
    byte[] getFile(FileRequest request) throws Exception;

    /**
     * 获取文件字输入流
     *
     * @param request
     * @return
     * @throws Exception
     */
    InputStream getFileAsStream(FileRequest request) throws Exception;

    /**
     * 流式处理方式，通过逐块地下载文件
     *
     * @param request
     * @param localPath
     * @throws Exception
     */
    void downloadFile(FileRequest request, String localPath) throws Exception;


    /**
     * 获取指定文件夹下的文件名列表
     *
     * @param request
     * @throws Exception
     */
    List<String> getFolderFileNames(FileRequest request) throws Exception;

    /**
     * 复制文件到指定目录
     * @param request
     * @throws Exception
     */
    void copyFile(FileCopyRequest request) throws Exception;

    /**
     * 获取文件大小
     * @param request
     * @throws Exception
     */
    long getFileSize(FileRequest request) throws Exception;
}
