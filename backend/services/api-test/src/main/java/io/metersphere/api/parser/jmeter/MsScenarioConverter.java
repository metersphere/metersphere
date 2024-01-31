package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.ApiScenarioParamConfig;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.scenario.ScenarioStepConfig;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 *
 * 脚本解析器
 */
public class MsScenarioConverter extends AbstractJmeterElementConverter<MsScenario> {

    @Override
    public void toHashTree(HashTree tree, MsScenario msScenario, ParameterConfig msParameter) {
        ApiScenarioParamConfig config = (ApiScenarioParamConfig) msParameter;


        ApiScenarioParamConfig chileConfig = getChileConfig(msScenario, config);

        parseChild(tree, msScenario, chileConfig);
    }

    /**
     * 获取子步骤的配置信息
     * 如果使用源场景环境，则使用当前场景的环境信息
     * @param msScenario
     * @param config
     * @return
     */
    private ApiScenarioParamConfig getChileConfig(MsScenario msScenario, ApiScenarioParamConfig config) {
        ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
        if (scenarioStepConfig != null && BooleanUtils.isTrue(scenarioStepConfig.getEnableScenarioEnv())) {
            // 使用源场景环境
            ApiScenarioParamConfig chileConfig = BeanUtils.copyBean(new ApiScenarioParamConfig(), config);
            chileConfig.setGrouped(msScenario.getGrouped());
            chileConfig.setEnvConfig(null);
            chileConfig.setProjectEnvMap(null);
            if (BooleanUtils.isTrue(msScenario.getGrouped())) {
                // 环境组设置环境Map
                Map<String, EnvironmentInfoDTO> projectEnvMap = msScenario.getProjectEnvMap();
                chileConfig.setProjectEnvMap(projectEnvMap);
            } else {
                // 设置环境信息
                EnvironmentInfoDTO environmentInfo = msScenario.getEnvironmentInfo();
                chileConfig.setEnvConfig(environmentInfo);
            }
            return chileConfig;
        }
        return config;
    }
}
