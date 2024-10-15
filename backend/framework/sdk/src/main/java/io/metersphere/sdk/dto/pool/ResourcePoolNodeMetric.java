package io.metersphere.sdk.dto.pool;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-10-14  18:28
 */
@Data
public class ResourcePoolNodeMetric {
    /**
     * 并发数
     */
    private Integer concurrentNumber;
    /**
     * 已占用的并发数
     */
    private Integer occupiedConcurrentNumber;
    /**
     * CPU使用率
     */
    private Double CPUUsage;
    /**
     * 内存使用率
     */
    private Double memoryUsage;
}
