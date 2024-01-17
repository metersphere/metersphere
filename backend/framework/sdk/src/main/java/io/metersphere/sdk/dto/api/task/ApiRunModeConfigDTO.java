package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiRunModeConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 运行模式 串行/并行
     */
    private String runMode;

    /**
     * 集合报告/独立报告
     */
    private String reportType;

    /**
     * 集合报告配置
     */
    private CollectionReportDTO collectionReport;

    /**
     * 失败停止
     */
    private boolean onSampleError;
    /**
     * 资源池
     */
    private String poolId;

    /**
     * 环境类型
     */
    private String environmentType;
    /**
     * 环境组id
     */
    private String environmentGroupId;
    /**
     * 执行环境id
     */
    private String environmentId;
}
