package io.metersphere.sdk.dto.api.task;

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
    private String msUrl;
    private String kafkaConfig;
    private String minioConfig;
    private int poolSize;
    /**
     * 批量执行时的队列ID
     */
    private String queueId;
    /**
     * 父队列 ID，即测试集队列 ID
     */
    private String parentQueueId;
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
    private String triggerMode;
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
}
