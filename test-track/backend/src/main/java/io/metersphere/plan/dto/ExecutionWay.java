package io.metersphere.plan.dto;

public enum ExecutionWay {
    /**
     * 仅运行
     */
    RUN,

    /**
     * 仅保存
     */
    SAVE,

    /**
     * Jenkins运行
     */
    JENKINS_RUN,

    /**
     * 运行保存
     */
    RUN_SAVE
}
