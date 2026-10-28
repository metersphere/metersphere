package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务项的执行资源清单
 * 服务端生成后按 taskToken 缓存，执行节点下载文件时只能访问清单中的文件
 */
@Data
public class ApiExecuteTaskResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务项ID
     */
    private String taskItemId;
    /**
     * 执行的资源ID
     */
    private String resourceId;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 可下载的文件列表
     * key: fileId
     */
    private Map<String, ApiExecuteFileResource> files = new HashMap<>();
}
