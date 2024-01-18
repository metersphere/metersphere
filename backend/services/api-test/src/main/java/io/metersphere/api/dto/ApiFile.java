package io.metersphere.api.dto;

import lombok.Data;

@Data
public class ApiFile {
    /**
     * 记录文件的ID，防止重名
     * 生成脚本时，通过 fileId + value(文件名) 获取文件路径
     */
    private String fileId;
    /**
     * 文件名称
     */
    private String fileName;
}
