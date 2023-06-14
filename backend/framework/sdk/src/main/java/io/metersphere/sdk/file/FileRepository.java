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
     * 获取文件字节内容
     *
     * @param request
     * @return
     * @throws Exception
     */
    public byte[] getFile(FileRequest request) throws Exception;

}
