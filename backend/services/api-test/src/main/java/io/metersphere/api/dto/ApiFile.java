package io.metersphere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 接口执行所需要的文件
 */
@Data
public class ApiFile {
    /**
     * 记录文件的ID，防止重名
     * 生成脚本时，通过 fileId + fileName(文件名) 获取文件路径
     */
    @NotBlank
    @Size(max = 50)
    private String fileId;
    /**
     * 文件名
     */
    @NotBlank
    @Size(max = 200)
    private String fileName;
}
