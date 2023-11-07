package io.metersphere.plugin.api.spi;

/**
 * 接口协议插件抽象类
 * @Author: jianxing
 * @CreateTime: 2023-11-06  11:10
 */
public abstract class AbstractProtocolPlugin extends AbstractApiPlugin {

    /**
     * 返回协议名称
     * @return
     */
    abstract public String getProtocol();
}
