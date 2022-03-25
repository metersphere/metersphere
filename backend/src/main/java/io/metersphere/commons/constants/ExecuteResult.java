package io.metersphere.commons.constants;

public enum ExecuteResult {
    //误报状态
    errorReportResult,

    //接口执行状态(兼容旧数据)
    success,error,

    //场景执行状态(兼容旧数据)
    Success,Error,

    //测试计划执行状态(兼容旧数据)
    PREPARE,RUNNING,SUCCESS,FAILD
}
