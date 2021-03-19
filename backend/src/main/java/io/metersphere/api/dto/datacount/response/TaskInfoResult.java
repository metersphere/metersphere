package io.metersphere.api.dto.datacount.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 任务信息 返回DTO
 */
@Getter
@Setter
public class TaskInfoResult {
    //序号
    private int index;
    //任务ID
    private String taskID;
    //任务名称
    private String name;
    //场景ID
    private String scenarioId;
    //规则
    private String rule;
    //任务状态
    private boolean taskStatus;
    //下次执行时间
    private Long nextExecutionTime;
    //创建人
    private String creator;
    //更新时间
    private Long updateTime;
    //定时任务类型  情景定时任务/范围计划任务
    private String taskType;
    //定时任务组别  swagger/scenario/testPlan 等
    private String taskGroup;

}
