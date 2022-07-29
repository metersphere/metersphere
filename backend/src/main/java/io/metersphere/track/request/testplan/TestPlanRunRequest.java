package io.metersphere.track.request.testplan;

import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanRunRequest {
    private String testPlanId;
    private String projectId;
    private String userId;
    private String triggerMode;//触发方式
    private String mode;//运行模式
    private String reportType;//报告展示方式
    private boolean onSampleError;//是否失败停止
    private boolean runWithinResourcePool;//是否选择资源池
    private String resourcePoolId;//资源池Id
    private Map<String, String> envMap;
    private String environmentType;
    private String environmentGroupId;
    private List<String> testPlanIds;
    private Boolean isAll;
    private String reportId;
    private QueryTestPlanRequest queryTestPlanRequest;

    // 失败重试
    private boolean retryEnable;
    private long retryNum;

    //ui 测试
    private String browser;
    private boolean headlessEnabled;
}

