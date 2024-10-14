package io.metersphere.sdk.dto.queue;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExecutionQueueDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源id，每个资源在同一个运行队列中唯一
     */
    private String resourceId;

    /**
     * 资源id，每个资源在同一个运行队列中唯一
     */
    private String taskItemId;

    /**
     * 排序
     */
    private Integer sort;
}