package io.metersphere.track.request.testplan;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestplanRunRequest {
    private String testPlanId;
    private String projectId;
    private String userId;
    private String triggerMode;//触发方式
    private String mode;//运行模式
    private String reportType;//报告展示方式
    private String onSampleError;//是否失败停止
    private String runWithinResourcePool;//是否选择资源池
    private String resourcePoolId;//资源池Id
}

