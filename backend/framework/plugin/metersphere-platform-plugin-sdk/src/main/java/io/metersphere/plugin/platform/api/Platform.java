package io.metersphere.plugin.platform.api;

import org.pf4j.ExtensionPoint;

/**
 * 平台对接相关业务接口
 * @author jianxing.chen
 */
public interface Platform extends ExtensionPoint {

    /**
     * 校验服务集成配置
     * 服务集成点击校验时调用
     */
    void validateIntegrationConfig();

    /**
     * 校验项目配置
     * 项目设置成点击校验项目 key 时调用
     */
    void validateProjectConfig(String projectConfig);
}
