package io.metersphere.sdk.dto.api.task;

import io.metersphere.sdk.dto.api.result.MsRegexDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务请求参数数据
 */
@Data
public class TaskRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String msUrl;
    private String kafkaConfig;
    private String minioConfig;
    private String poolId;
    private String queueId;
    /**
     * 是否需要实时接收单个步骤的结果
     */
    private Boolean realTime;

    /**
     * 执行的资源ID
     */
    private String testId;
    /**
     * 执行模式
     */
    private String runMode;
    /**
     * 资源类型
     * ApiResourceType
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
     * 误报规则
     */
    Map<String, List<MsRegexDTO>> fakeErrorMap;

    /**
     * 项目id
     */
    private String projectId;
    /**
     * 执行环境id
     */
    private List<String> environmentIds;

    // TODO 其它执行参数
}
