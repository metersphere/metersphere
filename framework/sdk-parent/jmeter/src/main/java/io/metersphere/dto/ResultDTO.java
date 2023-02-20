package io.metersphere.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResultDTO {
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
    // 失败重试
    private boolean retryEnable;
    /**
     * 增加一个全局扩展的通传参数
     */
    private Map<String, Object> extendedParameters;

    // 预留一个参数，可以放任何数据
    private Map<String, Object> arbitraryData;

}
