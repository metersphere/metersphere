package io.metersphere.plugin.platform.api;

/**
 * 平台对接相关业务接口
 * @author jianxing.chen
 */
public interface Platform {

    /**
     * 校验服务集成配置
     * 服务集成点击校验时调用
     */
    void validateIntegrationConfig();
}
