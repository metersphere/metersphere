package io.metersphere.system.dto;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  18:43
 */
@Data
public class ProtocolDTO {
    /**
     * 协议名
     */
    private String protocol;
    /**
     * 协议对应的组件名
     * 例如 Http 对应 MsHTTPElement
     */
    private String polymorphicName;
    /**
     * 插件ID
     */
    private String pluginId;
}
