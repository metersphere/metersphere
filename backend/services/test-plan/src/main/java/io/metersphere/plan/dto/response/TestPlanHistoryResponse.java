package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanHistoryResponse {

    @Schema(description = "报告ID")
    private String id;
    @Schema(description = "触发方式/执行方式")
    private String triggerMode;
    @Schema(description = "执行结果")
    private String resultStatus;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;
    @Schema(description = "是否还能查看执行报告")
    private boolean reportDeleted;
}
