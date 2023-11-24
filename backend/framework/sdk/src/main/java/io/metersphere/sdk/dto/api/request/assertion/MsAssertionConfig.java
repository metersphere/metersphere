package io.metersphere.sdk.dto.api.request.assertion;

import lombok.Data;

import java.util.List;

/**
 * 断言设置
 * @Author: jianxing
 * @CreateTime: 2023-11-23  17:26
 */
@Data
public class MsAssertionConfig {
    /**
     * 是否启用全局断言
     */
    private Boolean enableGlobal;
    /**
     * 断言列表
     */
    private List<MsAssertion> assertions;
}
