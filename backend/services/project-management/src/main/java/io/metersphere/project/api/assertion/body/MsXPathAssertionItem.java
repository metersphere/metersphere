package io.metersphere.project.api.assertion.body;


import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * XPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:18
 */
@Data
public class MsXPathAssertionItem extends MsBodyAssertionItem {
    /**
     * 表达式
     */
    private String expression;
    /**
     * 匹配值
     */
    private String expectedValue;

    public boolean isValid() {
        return StringUtils.isNotBlank(expression);
    }
}