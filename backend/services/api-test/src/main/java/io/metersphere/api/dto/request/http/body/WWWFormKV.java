package io.metersphere.api.dto.request.http.body;

import io.metersphere.api.dto.request.http.KeyValueEnableParam;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class WWWFormKV extends KeyValueEnableParam {
    /**
     * 参数类型
     *
     * @see WWWFormParamType
     */
    private String paramType;
    private Boolean required = false;
    private Integer minLength;
    private Integer maxLength;
    private String contentType;
    private Boolean encode = false;

    enum WWWFormParamType {
        /**
         * 默认 application/text
         */
        STRING, INTEGER, NUMBER, ARRAY,
        /**
         * 默认 application/octet-stream
         */
        FILE,
        /**
         * 默认 application/json
         */
        JSON
    }
}
