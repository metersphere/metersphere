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
    // 测试集Json
    private String testPlanCollectionJson;
    // 任务ID
    private String taskId;

    // 是否是队列的最后一个
    private boolean isLastOne = false;
    // 是否执行完毕
    private boolean executeFinish = false;
    // 是否是重新执行
    private boolean rerun = false;

    public TestPlanExecutionQueue(long pos, String createUser, long createTime, String queueId, String queueType, String parentQueueId, String parentQueueType, String sourceID, String runMode,
                                  String executionSource, String prepareReportId, String taskId, boolean rerun) {
        this.pos = pos;
        this.createUser = createUser;
        this.createTime = createTime;
        this.queueId = queueId;
        this.queueType = queueType;
        this.parentQueueId = parentQueueId;
        this.parentQueueType = parentQueueType;
        this.sourceID = sourceID;
        this.runMode = runMode;
        this.executionSource = executionSource;
        this.prepareReportId = prepareReportId;
        this.taskId = taskId;
        this.rerun = rerun;
    }
}
