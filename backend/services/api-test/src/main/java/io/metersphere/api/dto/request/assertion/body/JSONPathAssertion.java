package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;

import java.util.List;

/**
 *
 * JSONPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:04
 */
@Data
public class JSONPathAssertion {

    /**
     * 断言列表
     */
    private List<JSONPathAssertionItem> assertions;
}

