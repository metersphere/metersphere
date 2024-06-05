package io.metersphere.sdk.dto.queue;

import lombok.Data;

@Data
public class TestPlanExecutionQueue {
    private String queueId;
    private String testPlanId;
    private long pos;
    private String runMode;
    private String prepareReportId;
    private String createUser;
    private long createTime;
}
