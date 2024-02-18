package io.metersphere.plugin.api.spi;

/**
 * 接口协议插件抽象类
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-06  11:10
 */
public abstract class AbstractProtocolPlugin extends AbstractApiPlugin {

    private static final String DEFAULT_API_PROTOCOL_SCRIPT_ID = "api";
    private static final String DEFAULT_ENVIRONMENT_PROTOCOL_SCRIPT_ID = "environment";

    /**
     * 返回协议名
     *
     * @return 协议名
     */
    abstract public String getProtocol();

    /**
     * 返回接口协议主页面的脚本的ID，默认为 api
     * 可以重写此方法，返回自定义的脚本ID
     *
     * @return 协议id
     */
    public String getApiProtocolScriptId() {
        return DEFAULT_API_PROTOCOL_SCRIPT_ID;
    }

    /**
     * 返回协议环境配置页面的脚本的ID，默认为 environment
     * 可以重写此方法，返回自定义的脚本ID
     *
     * @return 脚本id
     */
    public String getEnvProtocolScriptId() {
        return DEFAULT_ENVIRONMENT_PROTOCOL_SCRIPT_ID;
    }
}
