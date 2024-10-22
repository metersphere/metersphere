package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExecTaskDetailDTO extends ApiScenarioReportDTO{
    @Schema(description = "发起时间")
    private Long createTime;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "任务来源")
    private String taskOrigin;
    @Schema(description = "任务来源名称")
    private String taskOriginName;


    @Schema(description = "结果")
    private String result;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "节点")
    private String resourcePoolNode;

    @Schema(description = "线程ID")
    private String threadId;

}
