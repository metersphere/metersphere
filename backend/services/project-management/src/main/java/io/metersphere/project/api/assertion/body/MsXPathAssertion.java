package io.metersphere.project.api.assertion.body;


import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

import java.util.List;

/**
 * XPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:18
 */
@Data
public class MsXPathAssertion {
    /**
     * 响应内容格式
     * 取值参考 {@link ResponseFormat}
     */
    @EnumValue(enumClass = ResponseFormat.class)
    private String responseFormat;
    /**
     * xpath断言列表
     */
    private List<MsXPathAssertionItem> assertions;

    public enum ResponseFormat {
        /**
         * XML
         */
        XML,
        /**
         * HTML
         */
        HTML
    }
}