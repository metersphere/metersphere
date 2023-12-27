package io.metersphere.plugin.api.dto;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  17:30
 */
@Data
public class ParameterConfig {
    private String reportId;
    /**
     * 解析时，是否解析 enable 为 false 的组件
     * 导出时，需要解析
     */
    private Boolean parseDisabledElement = false;
}
