package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import lombok.Data;

import java.util.List;

@Data
public class ParameterConfig {
    // 环境配置
    private EnvironmentConfig config;
    // 公共场景参数
    private List<ScenarioVariable> variables;
    // 公共Cookie
    private boolean enableCookieShare;
    // 步骤
    private String step;

    private String stepType;

}
