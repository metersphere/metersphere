package io.metersphere.functional.dto;

import lombok.Data;

@Data
public class FunctionalCaseAIDesignConfigDTO {
    //正常场景
    private Boolean normal;
    //异常场景
    private Boolean abnormal;
    //等价类划分
    private Boolean equivalenceClassPartitioning;
    //边界值分析
    private Boolean boundaryValueAnalysis;
    //决策表测试
    private Boolean decisionTableTesting;
    //因果图法
    private Boolean causeEffectGraphing;
    //正交实验法
    private Boolean orthogonalExperimentMethod;
    //场景法
    private Boolean scenarioMethod;
    //场景法描述
    private String scenarioMethodDescription;
}
