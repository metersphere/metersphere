package io.metersphere.api.dto.request.http;

import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

/**
 * rest参数
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class RestParam extends KeyValueEnableParam {
    /**
     * 参数类型
     * 取值参考 {@link KeyValueParamType}
     * 默认String
     */
    @EnumValue(enumClass = KeyValueParamType.class)
    private String paramType = KeyValueParamType.STRING.getValue();
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
