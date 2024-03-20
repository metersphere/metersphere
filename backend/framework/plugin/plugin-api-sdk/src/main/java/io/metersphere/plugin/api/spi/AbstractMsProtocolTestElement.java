package io.metersphere.plugin.api.spi;

import lombok.Data;

/**
 * 协议组件的抽象类
 * @Author: jianxing
 * @CreateTime: 2023-10-30  15:08
 */
@Data
public abstract class AbstractMsProtocolTestElement extends AbstractMsTestElement {
    /**
     * 是否是自定义请求
     */
    private Boolean customizeRequest = false;
    /**
     * 自定义请求是否启用环境
     */
    private Boolean customizeRequestEnvEnable = false;
}
