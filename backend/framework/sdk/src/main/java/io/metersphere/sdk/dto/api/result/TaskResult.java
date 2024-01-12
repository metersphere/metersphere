package io.metersphere.sdk.dto.api.result;

import io.metersphere.sdk.dto.api.CollectionReportDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // TODO: 补充任务执行结果数据结构

    private List<RequestResult> requestResults;
    private String runMode;
    private String queueId;
    private String reportId;
    private String reportType;
    private String testPlanReportId;
    private String testId;
    private String runType;
    private String console;
    private String runningDebugSampler;
    private Boolean hasEnded;
    private boolean retryEnable;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 执行过程状态
     */
    private ProcessResultDTO processResultDTO;
    /**
     * 资源类型
     * ApiResourceType
     */
    private String resourceType;
    /**
     * 环境变量处理信息
     */
    String environmentId;

    /**
     * 集合报告,空则是单报告
     */
    private CollectionReportDTO collectionReport;
}
