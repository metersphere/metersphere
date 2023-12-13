package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;

/**
 * 正则断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:03
 */
@Data
public class RegexAssertionItem extends BodyAssertionItem {
    private String expression;
}