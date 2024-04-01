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
     * 开启重试
     */
    private Boolean retryEnable;

    /**
     * 重试次数
     */
    private Long retryNumber;

    /**
     * 资源类型 / API，CASE，PLAN_CASE,PLAN_SCENARIO，API_SCENARIO
     * {@link io.metersphere.sdk.constants.ApiExecuteResourceType}
     */
    private String resourceType;

    /**
     * 运行模式配置
     */
    private ApiRunModeConfigDTO runModeConfig;

    /**
     * 全部场景的请求请求总量，用于计算执行各种指标
     */
    private Long requestCount;

    @Serial
    private static final long serialVersionUID = 1L;
}