package io.metersphere.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-26  17:53
 */
@Data
public class ResourceAddFileParam {
    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;

    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;
}
