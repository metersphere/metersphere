package io.metersphere.api.dto;

import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-30  10:25
 */
@Data
public class ApiScenarioParseEnvInfo {
    /**
     * 引用的场景的环境信息
     * key 为场景ID
     * value 为环境ID信息
     */
    private Map<String, EnvironmentModeDTO> refScenarioEnvMap;
    /**
     * 环境组信息
     * key 为环境组ID
     * value 为环境组下环境ID列表
     */
    private Map<String, List<String>> envGroupMap;
    /**
     *  环境信息
     *  key 为环境ID
     *  value 环境信息
     */
    Map<String, EnvironmentInfoDTO> envMap;
    /**
     * 环境配置信息
     * key 为 MsTestElement 实现类
     * value 为 对应插件的环境配置
     */
    private Map<Class<?>, Object> pluginClassEnvConfigMap;
}
