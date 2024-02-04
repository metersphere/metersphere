package io.metersphere.api.dto.assertion;

import io.metersphere.project.api.assertion.MsAssertion;
import lombok.Data;

import java.util.ArrayList;
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
     * 默认为 false
     */
    private Boolean enableGlobal = false;
    /**
     * 断言列表
     */
    private List<MsAssertion> assertions = new ArrayList<>(0);
}
