package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportDetail implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "报告fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(title = "场景中各个步骤请求唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "开始时间")
    private Long startTime;

    @Schema(title = "结果状态")
    private String status;

    @Schema(title = "单个请求步骤时间")
    private Long requestTime;

    @Schema(title = "总断言数")
    private Long assertionsTotal;

    @Schema(title = "失败断言数")
    private Long passAssertionsTotal;

    @Schema(title = "误报编号")
    private String fakeCode;

    @Schema(title = "请求名称")
    private String requestName;

    @Schema(title = "环境fk")
    private String environmentId;

    @Schema(title = "项目fk")
    private String projectId;

    @Schema(title = "失败总数")
    private Integer errorTotal;

    @Schema(title = "请求响应码")
    private String code;

    @Schema(title = "执行结果")
    private byte[] content;

    private static final long serialVersionUID = 1L;
}