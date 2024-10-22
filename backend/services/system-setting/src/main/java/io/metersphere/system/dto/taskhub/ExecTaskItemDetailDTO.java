package io.metersphere.system.dto.taskhub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ExecTaskItemDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String id;

    @Schema(description = "发起时间")
    private Long createTime;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "任务来源")
    private String taskOrigin;

    @Schema(description = "结果")
    private String result;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourcePoolId;

    @Schema(description = "节点")
    private String resourcePoolNode;

    @Schema(description = "线程ID")
    private String threadId;

    @Schema(description = "是否集合报告")
    private Boolean integrated;
}
