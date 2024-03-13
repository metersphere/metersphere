package io.metersphere.sdk.dto.api.task;

import io.metersphere.sdk.constants.ApiBatchRunMode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiRunModeConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 运行模式(串行/并行)
     * 是否并行执行
     * {@link io.metersphere.sdk.constants.ApiBatchRunMode}
     */
    private String runMode = ApiBatchRunMode.PARALLEL.name();

    /**
     * 是否是集成报告
     */
    private Boolean integratedReport = false;

    /**
     * 集合报告配置
     */
    private CollectionReportDTO collectionReport;

    /**
     * 失败停止
     */
    private Boolean stopOnFailure = false;

    /**
     * 资源池，为空则使用默认资源池
     */
    private String poolId;

    /**
     * 是否为环境组
     */
    private Boolean grouped = false;

    /**
     * 环境或者环境组ID
     */
    private String environmentId;

    public Boolean isParallel() {
        return StringUtils.equals(runMode, ApiBatchRunMode.PARALLEL.name());
    }
}
