package io.metersphere.sdk.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileRepository {
    /**
     * 保存文件
     *
     * @param file
     * @param request
     * @return
     * @throws Exception
     */
    public String saveFile(MultipartFile file, FileRequest request) throws Exception;

    /**
     * 保存文件
     *
     * @param bytes
     * @param request
     * @return
     * @throws Exception
     */
    public String saveFile(byte[] bytes, FileRequest request) throws Exception;

    /**
     * 删除文件
     *
     * @param request
     * @throws Exception
     */
    public void delete(FileRequest request) throws Exception;

    /**
     * 删除文件夹
     *
     * @param request
     * @throws Exception
     */
    public void deleteFolder(FileRequest request) throws Exception;


    /**
     * 获取文件字节内容，大文件不建议使用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public byte[] getFile(FileRequest request) throws Exception;

    /**
     * 流式处理方式，通过逐块地下载文件
     *
     * @param request
     * @param localPath
     * @throws Exception
     */
    public void downloadFile(FileRequest request, String localPath) throws Exception;

}
