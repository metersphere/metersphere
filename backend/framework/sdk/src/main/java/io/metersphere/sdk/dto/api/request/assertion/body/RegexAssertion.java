package io.metersphere.sdk.dto.api.request.assertion.body;

import lombok.Data;

import java.util.List;

/**
 * 正则断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:03
 */
@Data
public class RegexAssertion {
    /**
     * 断言列表
     */
    private List<RegexAssertionItem> assertions;
}