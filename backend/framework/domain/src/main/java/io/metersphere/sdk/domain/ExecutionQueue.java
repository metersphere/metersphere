package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ExecutionQueue implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "集合报告/测试计划报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue.report_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "报告类型/计划报告/单独报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue.report_type.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue.report_type.length_range}", groups = {Created.class, Updated.class})
    private String reportType;

    @Schema(title = "执行模式/scenario/api/test_paln_api/test_pan_scenario", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue.run_mode.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{execution_queue.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(title = "执行资源池", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{execution_queue.pool_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{execution_queue.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "")
    private Boolean failure;

    private static final long serialVersionUID = 1L;
}