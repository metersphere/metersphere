package io.metersphere.api.dto.request.assertion.body;

import lombok.Data;

import java.util.List;

/**
 * 正则断言
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:03
 */
@Data
public class MsRegexAssertion {
    /**
     * 断言列表
     */
    private List<MsRegexAssertionItem> assertions;
}