package io.metersphere.api.dto.request.assertion.body;


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
     * xml 或者 html
     */
    private String responseFormat;
    /**
     * xpath断言
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