package io.metersphere.sdk.dto.api.request.http.body;

import io.metersphere.sdk.dto.api.request.http.KeyValueEnableParam;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class FormDataKV extends KeyValueEnableParam {
    private String paramType;
    private Boolean required = false;
    private Integer minLength;
    private Integer maxLength;
    private String contentType;
    private Boolean encode = false;
    /**
     * 记录文件的ID，防止重名
     * 生成脚本时，通过 fileId + value(文件名) 获取文件路径
     */
    private String fileId;
}
