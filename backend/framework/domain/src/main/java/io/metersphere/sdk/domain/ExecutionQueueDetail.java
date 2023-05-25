package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ExecutionQueueDetail implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{execution_queue_detail.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "队列id")
    private String queueId;

    @Schema(title = "排序")
    private Integer sort;

    @Schema(title = "报告id")
    private String reportId;

    @Schema(title = "资源id")
    private String testId;

    @Schema(title = "资源类型")
    private String type;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "是否开启失败重试")
    private Boolean retryEnable;

    @Schema(title = "失败重试次数")
    private Long retryNumber;

    @Schema(title = "项目ID集合")
    private String projectIds;

    @Schema(title = "环境")
    private String evnMap;

    private static final long serialVersionUID = 1L;
}