package io.metersphere.sdk.dto.api.request.http;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class QueryParam extends KeyValueEnableParam {

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
    /**
     * 最大长度
     */
    private Integer minLength;
    /**
     * 最小长度
     */
    private Integer maxLength;
    /**
     * 是否编码
     */
    private Boolean encode = false;
}
