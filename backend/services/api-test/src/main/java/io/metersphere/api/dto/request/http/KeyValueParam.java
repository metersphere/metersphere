package io.metersphere.api.dto.request.http;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  17:27
 */
@Data
public class KeyValueParam {
    /**
     * 键
     */
    private String key;
    /**
     * 值
     */
    private String value;
}
