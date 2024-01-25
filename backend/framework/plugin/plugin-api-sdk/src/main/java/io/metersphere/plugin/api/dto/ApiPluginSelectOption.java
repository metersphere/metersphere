package io.metersphere.plugin.api.dto;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-24  17:47
 */
@Data
public class ApiPluginSelectOption {
    /**
     * 选项名称
     */
    private String text;
    /**
     * 选项值
     */
    private String value;
}
