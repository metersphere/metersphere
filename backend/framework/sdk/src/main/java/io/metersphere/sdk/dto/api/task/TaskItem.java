package io.metersphere.sdk.dto.api.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务项
 */
@Data
public class TaskItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务项ID
     */
    @NotBlank
    private String id;

    private String reportId;
    /**
     * 执行的资源ID
     */
    @NotBlank
    private String resourceId;
    /**
     * 待执行任务所需要的文件资源
     */
    private TaskResourceFile taskResourceFile = new TaskResourceFile();
    /**
     * 场景执行时关联的其他项目的步骤所需的资源
     */
    private TaskProjectResource refProjectResource = new TaskProjectResource();
    /**
     * 要执行的请求总量，用于计算执行各种指标
     */
    private Long requestCount;
}
