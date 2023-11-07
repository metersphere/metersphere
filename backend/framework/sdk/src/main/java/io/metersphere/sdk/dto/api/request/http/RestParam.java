package io.metersphere.sdk.dto.api.request.http;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class RestParam extends KeyValueParam {
    /**
     * 参数类型
     * 默认string，可选integer、number、array
     * todo
     */
    private String paramType;
    /**
     * 是否必填
     */
    private Boolean required = false;
}
