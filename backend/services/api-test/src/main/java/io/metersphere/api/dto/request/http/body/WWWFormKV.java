package io.metersphere.api.dto.request.http.body;

import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.system.valid.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * x-www-form-urlencoded 请求体键值对
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class WWWFormKV extends KeyValueEnableParam {
    /**
     * 参数类型
     * 取值参考 {@link BodyParamType} 中的 value 属性
     */
    @NotBlank
    @Size(max = 20)
    @EnumValue(enumClass = BodyParamType.class)
    private String paramType;
    /**
     * 是否必填
     * 默认为 false
     */
    private Boolean required = false;
    /**
     * 最小长度
     */
    private Integer minLength;
    /**
     * 最大长度
     */
    private Integer maxLength;
    /**
     * 是否对参数进行编码
     * 默认 false
     */
    private Boolean encode = false;
}
