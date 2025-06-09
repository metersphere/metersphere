package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAIDesignConfigDTO {
    //正常场景
    @Schema(description = "正常场景")
    private Boolean normal;
    //异常场景
    @Schema(description = "异常场景")
    private Boolean abnormal;
    //等价类划分
    @Schema(description = "等价类划分")
    private Boolean equivalenceClassPartitioning;
    //边界值分析
    @Schema(description = "边界值分析")
    private Boolean boundaryValueAnalysis;
    //决策表测试
    @Schema(description = "决策表测试")
    private Boolean decisionTableTesting;
    //因果图法
    @Schema(description = "因果图法")
    private Boolean causeEffectGraphing;
    //正交实验法
    @Schema(description = "正交实验法")
    private Boolean orthogonalExperimentMethod;
    //场景法
    @Schema(description = "场景法")
    private Boolean scenarioMethod;
    //场景法描述
    @Schema(description = "场景法描述")
    private String scenarioMethodDescription;

    public FunctionalCaseAIDesignConfigDTO() {
        // 默认构造函数
        this.normal = true;
        this.abnormal = true;
        this.equivalenceClassPartitioning = true;
        this.boundaryValueAnalysis = true;
        this.decisionTableTesting = true;
        this.causeEffectGraphing = true;
        this.orthogonalExperimentMethod = true;
        this.scenarioMethod = true;
        this.scenarioMethodDescription = "";
    }
}
