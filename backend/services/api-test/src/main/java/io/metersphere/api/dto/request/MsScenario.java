package io.metersphere.api.dto.request;

import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.api.dto.scenario.ScenarioStepConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsScenario extends AbstractMsTestElement {
    /**
     * 场景配置
     * 引用的场景才会设置
     */
    private ScenarioConfig scenarioConfig;
    /**
     * 场景步骤的配置
     * 引用的场景才会设置
     */
    private ScenarioStepConfig scenarioStepConfig;
    /**
     * 环境 Map
     * key 为项目ID
     * value 为环境信息
     * 引用的场景才会设置
     */
    private Map<String, EnvironmentInfoDTO> projectEnvMap;
    /**
     * 环境信息
     * 引用的场景才会设置
     */
    private EnvironmentInfoDTO environmentInfo;
    /**
     * 是否为环境组
     * 是则使用 projectEnvMap
     * 否则使用 envInfo
     * 引用的场景才会设置
     */
    private Boolean grouped;
    /**
     * {@link io.metersphere.api.constants.ApiScenarioStepRefType}
     * DIRECT 表示当前根场景
     */
    private String refType;
}
