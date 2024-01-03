package io.metersphere.api.dto.request.assertion.body;


import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * XPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:18
 */
@Data
public class MsXPathAssertionItem extends MsBodyAssertionItem {
    private String expression;
    private String expectedValue;

    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && StringUtils.isNotBlank(expectedValue);
    }
}