package io.metersphere.sdk.dto.api.request.http.body;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class JsonBody {
    /**
     * 是否启用 json-schema
     */
    private Boolean enableJsonSchema;
    /**
     * 没有启用 json-schema 时的 json 参数值
     */
    private String jsonValue;
    /**
     * 启用 json-schema 时的参数对象
     * todo json-schema 编辑器待调研，暂时使用 Object 类型
     */
    private Object jsonSchema;
}
