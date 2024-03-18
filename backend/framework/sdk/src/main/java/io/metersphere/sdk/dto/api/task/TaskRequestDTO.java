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
public class TaskRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String reportId;
    private String msUrl;
    private String kafkaConfig;
    private String minioConfig;
    private int poolSize;
    /**
     * 批量执行时的队列ID
     */
    private String queueId;
    /**
     * 是否需要实时接收单个步骤的结果
     */
    private Boolean realTime = false;
    /**
     * 是否保存执行结果
     */
    private Boolean saveResult = true;
    /**
     * 执行的资源ID
     */
    @NotBlank
    private String resourceId;
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
     * 点击调试时，尚未保存的本地上传的文件列表
     */
    private List<ApiExecuteFileInfo> localTempFiles;
    /**
     * 通过本地上传的文件ID列表
     */
    private List<ApiExecuteFileInfo> localFiles;
    /**
     * 关联文件管理的文件列表
     * 这里记录文件名，mino存的文件名是id
     * 执行时下载文件后，按原文件命名
     */
    private List<ApiExecuteFileInfo> refFiles;
    /**
     * 插件文件列表
     * id 为插件的 id + 更新时间戳
     */
    private List<ApiExecuteFileInfo> pluginFiles;

    /**
     * 接口测试函数包
     */
    private List<ApiExecuteFileInfo> funcJars;

    /**
     * 误报规则
     */
    List<MsRegexDTO> msRegexList;
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
     * TODO 要执行的请求总量，用于计算执行各种指标
     */
    private Long requestCount;

    /**
     * 记录执行时的环境变量
     */
    private List<String> environmentVariables;

    // TODO 其它执行参数
}
