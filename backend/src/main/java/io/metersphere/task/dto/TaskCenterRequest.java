package io.metersphere.task.dto;

import lombok.Data;

@Data
public class TaskCenterRequest {

    private String projectId;
    /**
     * 触发方式
     */
    private String triggerMode;
    /**
     * 执行状态
     */
    private String executionStatus;
    /**
     * 执行人
     */
    private String executor;
}
