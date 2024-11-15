package io.metersphere.sdk.dto.api.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 获取执行脚本请求参数
 */
@Data
public class GetRunScriptRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 任务项
     */
    private TaskItem taskItem;
    /**
     * 操作人
     */
    private String userId;
    /**
     * {@link io.metersphere.sdk.constants.ApiExecuteRunMode}
     */
    @NotBlank
    private String runMode;
    /**
     * 运行配置
     */
    @Valid
    private ApiRunModeConfigDTO runModeConfig = new ApiRunModeConfigDTO();
    /**
     * 资源类型
     *
     * @see io.metersphere.sdk.constants.ApiExecuteResourceType
     */
    private String resourceType;
    /**
     * 是否需要解析脚本
     * 接口详情页面，需要传试试详情，会其他解析脚本，needParseScript 为 false
     * 不传详情执行时，通过 task-runner 发起解析脚本请求，needParseScript 为 true
     */
    private Boolean needParseScript = false;
    /**
     * 线程ID
     */
    private String threadId;
    /**
     * 是否是批量执行
     * 包括用例的批量执行
     * 测试计划的执行
     */
    private Boolean batch;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 资源池ID
     */
    private String poolId;
    /**
     * 触发方式
     * 手动执行，批量执行，API执行，定时任务
     * {@link io.metersphere.sdk.constants.TaskTriggerMode}
     */
    private String triggerMode;
    /**
     * 是否是任务失败重跑
     */
    private Boolean rerun = false;
}
