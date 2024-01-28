package io.metersphere.api.dto.request.processors;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * 前后置处理器配置
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:17
 */
@Data
public class MsProcessorConfig {
    /**
     * 是否启用全局前置
     * 默认为 false
     */
    private Boolean enableGlobal = false;
    /**
     * 处理器列表
     */
    @Valid
    private List<MsProcessor> processors;
}
