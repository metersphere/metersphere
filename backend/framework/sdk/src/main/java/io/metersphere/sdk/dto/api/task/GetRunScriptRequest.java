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
}
