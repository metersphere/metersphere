package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
    /**
     * 是否需要实时接收单个步骤的结果
     */
    private Boolean realTime;

    /**
     * 执行的资源ID
     */
    private String testId;
    /**
     * 点击调试时，尚未保存的文件ID列表
     */
    private List<String> tempFileIds;
    /**
     * 执行模式
     */
    private String runMode;
    /**
     * 资源类型
     */
    private String resourceType;


    // TODO 其它执行参数
}
