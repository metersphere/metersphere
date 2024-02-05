package io.metersphere.api.dto;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-29  15:09
 */
@Data
public class ApiScenarioParamConfig extends ApiParamConfig {
    /**
     * 环境 Map
     * key 为项目ID
     * value 为环境信息
     */
    private Map<String, EnvironmentInfoDTO> projectEnvMap;
    /**
     * 是否为环境组
     * 是则使用 projectEnvMap
     * 否则使用 envInfo
     */
    private Boolean grouped;

    @Override
    public Map<String, Object> getProtocolEnvConfig(AbstractMsTestElement msTestElement) {
        if (BooleanUtils.isTrue(grouped)) {
            return getProtocolEnvConfig(msTestElement, projectEnvMap.get(msTestElement.getProjectId()));
        } else {
            return super.getProtocolEnvConfig(msTestElement);
        }
    }

    /**
     * 获取当前环境或者环境组的配置
     * @param projectId
     * @return
     */
    @Override
    public EnvironmentInfoDTO getEnvConfig(String projectId) {
        if (BooleanUtils.isTrue(grouped)) {
            return projectEnvMap.get(projectId);
        } else {
            return getEnvConfig();
        }
    }
}
