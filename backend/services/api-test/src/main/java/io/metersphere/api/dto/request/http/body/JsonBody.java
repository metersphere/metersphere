package io.metersphere.api.dto.request.http.body;

import io.metersphere.api.dto.schema.JsonSchemaItem;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * json 请求体
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */
@Data
public class JsonBody {
    /**
     * 是否启用 json-schema
     * 默认false
     */
    private Boolean enableJsonSchema = false;
    /**
     * json 参数值
     * 当 enableJsonSchema 为 false 时使用该值
     */
    private String jsonValue;
    /**
     * 启用 json-schema 时的参数对象
     * 当 enableJsonSchema 为 true 时使用该值
     */
    @Valid
    private JsonSchemaItem jsonSchema;
    /**
     * 是否开启动态转换
     * 默认为 false
     */
    private Boolean enableTransition = false;
}
