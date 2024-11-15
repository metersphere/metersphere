package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExecuteReportDTO implements Serializable {
    @Schema(description = "接口报告pk")
    private String id;

    @Schema(description = "序号")
    private String num;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "操作人")
    private String operationUser;
    @Schema(description = "操作人id")
    private String createUser;

    @Schema(description = "操作时间")
    private Long startTime;

    @Schema(description = "报告结果/SUCCESS/ERROR")
    private String status;

    @Schema(description = "报告状态")
    private String execStatus;

    @Schema(description = "执行方式")
    private String triggerMode;

    @Schema(description = "是否删除")
    private boolean deleted;

    @Schema(description = "执行历史是否被清理")
    private boolean historyDeleted = false;

    @Schema(description = "是否集成")
    private boolean integrated;

    @Schema(description = "测试计划id")
    private String testPlanId;

    @Schema(description = "测试计划Num")
    private String testPlanNum;

    @Schema(description = "结果是否被删除")
    private Boolean resultDeleted = true;

    private static final long serialVersionUID = 1L;

}