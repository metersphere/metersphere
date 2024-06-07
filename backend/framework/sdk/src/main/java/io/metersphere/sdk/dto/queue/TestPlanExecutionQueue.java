package io.metersphere.sdk.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanExecutionQueue {
    private String queueId;
    private String parentQueueId;
    private String testPlanId;
    //顺序
    private long pos;
    //执行模式
    private String runMode;
    //执行来源
    private String executionSource;
    private String prepareReportId;
    private String createUser;
    private long createTime;
}
