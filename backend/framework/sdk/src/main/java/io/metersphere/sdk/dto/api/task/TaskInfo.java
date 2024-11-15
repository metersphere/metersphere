package io.metersphere.sdk.dto.api.task;

import io.metersphere.sdk.constants.TaskTriggerMode;
import io.metersphere.sdk.dto.api.result.MsRegexDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 任务请求参数数据
 */
@Data
public class TaskInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;
    private String msUrl;
    private String kafkaConfig;
    private String minioConfig;
    /**
     * 单个任务的并发数
     */
    private int perTaskSize;
    /**
     * 资源池的并发数
     */
    private int poolSize;
    /**
     * 批量，串行时的队列ID
     */
    private String queueId;
    /**
     * 批量，并行时的集合ID
     */
    private String setId;
    /**
     * 父队列 ID，即测试集队列 ID
     */
    private String parentQueueId;
    /**
     * 父集合 ID，用于测试计划用例批量执行
     * 其中测试集并行执行时，需要记录所有所有的任务项，以判断执行完成
     */
    private String parentSetId;
    /**
     * 是否需要实时接收单个步骤的结果
     */
    private Boolean realTime = false;
    /**
     * 是否保存执行结果
     */
    private Boolean saveResult = true;
    /**
     * 是否需要解析脚本
     * 接口详情页面，需要传试试详情，会其他解析脚本，needParseScript 为 false
     * 不传详情执行时，通过 task-runner 发起解析脚本请求，needParseScript 为 true
     */
    private Boolean needParseScript = true;
    /**
     * 操作人
     */
    private String userId;
    /**
     * 触发方式
     * 手动执行，批量执行，API执行，定时任务
     * {@link io.metersphere.sdk.constants.TaskTriggerMode}
     */
    private String triggerMode = TaskTriggerMode.MANUAL.name();
    /**
     * 资源类型
     *
     * @see io.metersphere.sdk.constants.ApiExecuteResourceType
     */
    private String resourceType;

    /**
     * 当前项目执行时所需的资源
     */
    private TaskProjectResource projectResource = new TaskProjectResource();

    /**
     * 误报规则
     */
    private List<MsRegexDTO> msRegexList;
    /**
     * 项目id
     */
    @NotBlank
    private String projectId;

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
     * 记录执行时的环境变量
     */
    private List<String> environmentVariables;

    /**
     * 是否是批量执行
     * 包括用例的批量执行
     * 测试计划的执行
     */
    private Boolean batch = false;
    /**
     * 资源池ID
     * 执行时初始化报告需要记录资源池ID
     */
    private String poolId;
    /**
     * 是否是任务失败重跑
     */
    private Boolean rerun = false;
}
