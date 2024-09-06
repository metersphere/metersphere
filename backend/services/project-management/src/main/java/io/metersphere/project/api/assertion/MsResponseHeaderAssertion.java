package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 响应头断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_HEADER")
public class MsResponseHeaderAssertion extends MsAssertion {
    /**
     * 断言列表
     */
    @Valid
    private List<ResponseHeaderAssertionItem> assertions;


    /**
     * 响应头断言项
     */
    @Data
    public static class ResponseHeaderAssertionItem {
        /**
         * 是否启用
         * 默认启用
         */
        private Boolean enable = true;
        /**
         * 响应头
         */
        private String header;
        /**
         * 匹配条件
         * 取值参考 {@link io.metersphere.sdk.constants.MsAssertionCondition}
         */
        @NotBlank
        @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
        private String condition;
        /**
         * 匹配值
         */
        private String expectedValue;
    }
}
