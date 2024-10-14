package io.metersphere.plugin.api.dto;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;

import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  17:30
 */
@Data
public abstract class ParameterConfig {
    /**
     * 任务项的唯一ID
     */
    private String taskItemId;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 解析时，是否解析 enable 为 false 的组件
     * 导出时，需要解析
     */
    private Boolean parseDisabledElement = false;
    /**
     * 获取当前插件对应的环境配置
     * 调用示例： getProtocolEnvConfig(msTestElement);
     * @param msTestElement 当前 AbstractMsTestElement 的实现类对象
     * @return
     */
    public abstract Map<String, Object> getProtocolEnvConfig(AbstractMsTestElement msTestElement);
}
