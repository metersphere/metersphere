package io.metersphere.api.dto.request.assertion.body;


import lombok.Data;
/**
 * XPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:18
 */
@Data
public class XPathAssertionItem extends BodyAssertionItem {
    private String expression;
    private String condition;
    private String value;
}