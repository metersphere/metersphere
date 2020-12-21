package io.metersphere.api.dto.dataCount;

import lombok.Getter;
import lombok.Setter;

/**
 * 已执行的案例
 */
@Getter
@Setter
public class ExecutedCaseInfoResult {
    //案例名称
    private String caseName;
    //所属测试计划
    private String testPlan;
    //失败次数
    private Long failureTimes;
    //案例类型
    private String caseType;
}
