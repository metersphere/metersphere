package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class ExecutionModeDTO {

    /**
     * 运行模式
     */
    private String runMode;

    /**
     * 资源池
     */
    private String resourcePool;
}
