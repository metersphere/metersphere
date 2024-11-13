package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ExecHistoryDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务项id")
    private String itemId;

    @Schema(description = "任务id")
    private String taskId;

    @Schema(description = "执行方式")
    private String triggerMode;

    @Schema(description = "执行结果")
    private String status;

    @Schema(description = "状态")
    private String execStatus;

    @Schema(description = "操作人")
    private String createUser;

    @Schema(description = "操作时间")
    private Long startTime;

    @Schema(description = "是否集合报告")
    private Boolean integrated;

    @Schema(description = "测试计划id")
    private String testPlanId;

    @Schema(description = "测试计划Num")
    private String testPlanNum;
}
