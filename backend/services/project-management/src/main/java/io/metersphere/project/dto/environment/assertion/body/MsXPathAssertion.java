package io.metersphere.project.dto.environment.assertion.body;


import lombok.Data;

import java.util.List;

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