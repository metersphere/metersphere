package io.metersphere.sdk.dto.queue;

import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExecutionQueue implements Serializable {
    /**
     * 队列ID, 用于区分不同的队列,如果测试计划执行，队列ID为测试计划报告ID
     */
    private String queueId;

    /**
     * 执行人
     */
    private String userId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 父队列 ID，即测试集队列 ID
     */
    private String parentQueueId;

    /**
     * 资源类型 / API，CASE，PLAN_CASE,PLAN_SCENARIO，API_SCENARIO
     * {@link io.metersphere.sdk.constants.ApiExecuteResourceType}
     */
    private String resourceType;

    /**
     * 运行模式配置
     */
    private ApiRunModeConfigDTO runModeConfig;

    @Serial
    private static final long serialVersionUID = 1L;
}