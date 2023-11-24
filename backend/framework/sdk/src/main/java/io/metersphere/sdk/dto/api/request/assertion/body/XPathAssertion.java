package io.metersphere.sdk.dto.api.request.assertion.body;


import lombok.Data;

import java.util.List;

/**
 * XPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:18
 */
@Data
public class XPathAssertion {
    /**
     * 响应内容格式
     * xml 或者 html
     */
    private String format;
    /**
     * xpath断言
     */
    private List<XPathAssertionItem> assertions;
}