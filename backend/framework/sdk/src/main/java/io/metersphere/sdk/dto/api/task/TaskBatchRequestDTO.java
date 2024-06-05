package io.metersphere.sdk.dto.api.task;

import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 任务请求参数数据
 */
@Data
public class TaskBatchRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务的基本信息
     */
    @Valid
    private TaskInfo taskInfo = new TaskInfo();

    /**
     * 任务项
     */
    @Valid
    private List<TaskItem> taskItems;
}
