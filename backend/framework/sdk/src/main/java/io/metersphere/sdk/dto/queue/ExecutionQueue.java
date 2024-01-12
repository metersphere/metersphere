package io.metersphere.sdk.dto.queue;

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
     * 报告类型/测试计划类型/测试用例类型/测试集类型/场景集合类型
     */
    private String reportType;

    /**
     * 运行模式
     */
    private String runMode;

    /**
     * 资源池ID
     */
    private String poolId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 是否失败继续
     */
    private Boolean failure;

    /**
     * 开启重试
     */
    private Boolean retryEnable;

    /**
     * 重试次数
     */
    private Long retryNumber;

    /**
     * 环境Id
     */
    private String environmentId;

    @Serial
    private static final long serialVersionUID = 1L;
}