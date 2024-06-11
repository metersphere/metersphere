package io.metersphere.sdk.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestPlanExecutionQueue {
    //顺序
    private long pos;
    //创建人
    private String createUser;
    //创建时间
    private long createTime;
    //队列ID
    private String queueId;
    // 队列类型
    private String queueType;
    //父类队列ID
    private String parentQueueId;
    //父类队列ID
    private String parentQueueType;
    //资源ID（测试集/测试计划/测试计划组)
    private String sourceID;
    //执行模式
    private String runMode;
    //执行来源
    private String executionSource;
    //预生成报告ID
    private String prepareReportId;

    private boolean lastFinished = false;
}
