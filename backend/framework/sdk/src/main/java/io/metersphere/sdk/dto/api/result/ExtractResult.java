package io.metersphere.sdk.dto.api.result;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-05-09  17:08
 */
@Data
public class ExtractResult {
    private String name;
    private String value;
    private String type;
    private String expression;
}
