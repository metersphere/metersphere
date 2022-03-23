package io.metersphere.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskCenterRequest {

    private String projectId;

    private String userId;

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

    private int goPage;

    private int pageSize;

    List<String> projects;
}
