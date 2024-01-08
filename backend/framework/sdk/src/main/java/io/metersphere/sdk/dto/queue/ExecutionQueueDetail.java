package io.metersphere.sdk.dto.queue;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class ExecutionQueueDetail implements Serializable {
    /**
     * 资源id，每个资源在同一个运行队列中唯一
     */
    private String resourceId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 当前资源产生的执行报告id
     */
    private String reportId;

    /**
     * 资源类型 / API，CASE，PLAN_CASE,PLAN_SCENARIO，API_SCENARIO
     */
    private String resourceType;

    /**
     * 环境，key= projectID，value=envID，优先使用Queue上的环境，如果没有则使用资源上的环境
     */
    private Map<String, String> envMap;

    @Serial
    private static final long serialVersionUID = 1L;
}