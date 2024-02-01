package io.metersphere.project.dto.environment;

import io.metersphere.project.api.assertion.MsAssertion;
import lombok.Data;

import java.util.List;

/**
 * 断言设置
 * @Author: jianxing
 * @CreateTime: 2023-11-23  17:26
 */
@Data
public class MsEnvAssertionConfig {
    /**
     * 断言列表
     */
    private List<MsAssertion> assertions;
}
