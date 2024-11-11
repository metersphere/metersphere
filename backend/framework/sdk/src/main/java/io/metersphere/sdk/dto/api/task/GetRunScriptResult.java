package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 获取执行脚本请求结果
 */
@Data
public class GetRunScriptResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 执行脚本
     */
    private String script;

    /**
     * 要执行的请求总量，用于计算执行各种指标
     */
    private Long requestCount;
    /**
     * 执行所需的文件
     */
    private TaskResourceFile taskResourceFile;
    /**
     * 场景执行时关联的其他项目的步骤所需的资源
     */
    private TaskProjectResource refProjectResource = new TaskProjectResource();

}
