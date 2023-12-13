package io.metersphere.sdk.dto.api.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> arbitraryData;
    Map<String, List<MsRegexDTO>> fakeErrorMap;
}
