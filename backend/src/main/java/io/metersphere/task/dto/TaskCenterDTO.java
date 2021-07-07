package io.metersphere.task.dto;

import lombok.Data;

@Data
public class TaskCenterDTO {
    /**
     * 执行对象名称
     */
    private String name;

    private String id;
    /**
     * 执行器
     */
    private String actuator;
    /**
     * 执行人
     */
    private String executor;
    /**
     * 执行时间
     */
    private Long executionTime;
    /**
     * 触发方式
     */
    private String triggerMode;
    /**
     * 执行状态
     */
    private String executionStatus;
    /**
     * 执行模块/场景/接口/性能
     */
    private String executionModule;
}
