package io.metersphere.api.dto.request.http;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 可以启用禁用的键值对参数
 * @Author: jianxing
 * @CreateTime: 2023-11-06  17:27
 */
@Data
public class KeyValueEnableParam extends KeyValueParam {
    /**
     * 是否启用
     * 默认启用
     */
    private Boolean enable = true;
    /**
     * 描述
     */
    @Size(max = 500)
    private String description;
}
