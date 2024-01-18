package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.request.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-12  09:47
 */
@Data
public class ScenarioConfig {
    /**
     * 场景变量
     */
    private ScenarioVariable variable;
    /**
     * 前置处理器配置
     */
    private MsProcessorConfig preProcessorConfig;
    /**
     * 后置处理器配置
     */
    private MsProcessorConfig postProcessorConfig;
    /**
     * 断言配置
     */
    private MsAssertionConfig assertionConfig;
    /**
     * 其他配置
     */
    private ScenarioOtherConfig otherConfig;
}
