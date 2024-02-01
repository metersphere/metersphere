package io.metersphere.project.api.assertion.body;

import lombok.Data;

import java.util.List;

/**
 *
 * JSONPath断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:04
 */
@Data
public class MsJSONPathAssertion {

    /**
     * 断言列表
     */
    private List<MsJSONPathAssertionItem> assertions;
}

