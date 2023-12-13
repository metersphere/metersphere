package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;

/**
 *
 * JSONPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:04
 */
@Data
public class JSONPathAssertionItem extends BodyAssertionItem {
    private String expression;
    private String condition;
    private String value;
}

