package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import lombok.Data;

import java.util.List;

@Data
public class ParameterConfig {
    // 环境配置
    private EnvironmentConfig config;
    // 公共场景参数
    private List<KeyValue> variables;
    // 公共Cookie
    private boolean enableCookieShare;

}
