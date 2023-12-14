package io.metersphere.sdk.file;

import lombok.Data;

/**
 * 复制文件请求
 */
@Data
public class FileCopyRequest extends FileRequest {
    /**
     * 复制的文件目录
     */
    private String copyFolder;

    /**
     * 复制的文件名称
     */
    private String copyfileName;
}