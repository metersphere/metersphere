package io.metersphere.sdk.dto.api.request.processors;

import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:17
 */
@Data
public class MsProcessorConfig {
    /**
     * 是否启用全局前置
     */
    private Boolean enableGlobal;
    /**
     * 处理器
     */
    private List<MsProcessor> processors;
}
