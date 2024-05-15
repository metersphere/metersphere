package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.assertion.MsScenarioAssertionConfig;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import jakarta.validation.Valid;
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
    @Valid
    private ScenarioVariable variable = new ScenarioVariable();
    /**
     * 前置处理器配置
     */
    private MsProcessorConfig preProcessorConfig = new MsProcessorConfig();
    /**
     * 后置处理器配置
     */
    private MsProcessorConfig postProcessorConfig = new MsProcessorConfig();
    /**
     * 断言配置
     */
    private MsScenarioAssertionConfig assertionConfig = new MsScenarioAssertionConfig();
    /**
     * 其他配置
     */
    private ScenarioOtherConfig otherConfig = new ScenarioOtherConfig();
}
