package io.metersphere.ui.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReportDetail implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "资源id（场景，接口）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "报告 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{ui_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{ui_scenario_report_detail.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{ui_scenario_report_detail.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "请求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report_detail.request_time.not_blank}", groups = {Created.class})
    private Long requestTime;

    @Schema(title = "总断言数")
    private Long totalAssertions;

    @Schema(title = "失败断言数")
    private Long passAssertions;

    @Schema(title = "执行结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{ui_scenario_report_detail.content.not_blank}", groups = {Created.class})
    private byte[] content;

    @Schema(title = "记录截图断言等结果")
    private byte[] baseInfo;

    private static final long serialVersionUID = 1L;
}