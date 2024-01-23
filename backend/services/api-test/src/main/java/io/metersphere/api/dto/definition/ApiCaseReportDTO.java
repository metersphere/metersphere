package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiCaseReportDTO implements Serializable {
    @Schema(description = "接口报告pk")
    private String id;

    @Schema(description = "序号")
    private String num;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "用例id")
    private String resourceId;

    @Schema(description = "测试计划id")
    private String testPlanId;

    @Schema(description = "操作人")
    private String operationUser;
    @Schema(description = "操作人id")
    private String createUser;

    @Schema(description = "操作时间")
    private Long startTime;

    @Schema(description = "报告状态/SUCCESS/ERROR")
    private String status;

    @Schema(description = "执行方式")
    private String triggerMode;

    @Schema(description = "是否是测试计划执行的用例")
    private boolean testPlan;

    private static final long serialVersionUID = 1L;

}