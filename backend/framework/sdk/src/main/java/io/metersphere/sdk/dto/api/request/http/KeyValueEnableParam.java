package io.metersphere.sdk.dto.api.request.http;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  17:27
 */
@Data
public class KeyValueEnableParam extends KeyValueParam {
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 描述
     */
    private String description;
}
