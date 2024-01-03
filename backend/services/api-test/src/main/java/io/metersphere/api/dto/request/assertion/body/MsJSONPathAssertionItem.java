package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * JSONPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:04
 */
@Data
public class MsJSONPathAssertionItem extends MsBodyAssertionItem {
    private String expression;
    private String condition;
    private String expectedValue;


    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(expectedValue);
    }
}

