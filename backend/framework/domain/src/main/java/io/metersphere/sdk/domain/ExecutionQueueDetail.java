package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ExecutionQueueDetail implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "队列id")
    private String queueId;

    @Schema(description =  "排序")
    private Integer sort;

    @Schema(description =  "报告id")
    private String reportId;

    @Schema(description =  "资源id")
    private String testId;

    @Schema(description =  "资源类型")
    private String type;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "是否开启失败重试")
    private Boolean retryEnable;

    @Schema(description =  "失败重试次数")
    private Long retryNumber;

    @Schema(description =  "项目ID集合")
    private String projectIds;

    @Schema(description =  "环境")
    private String evnMap;

    private static final long serialVersionUID = 1L;
}