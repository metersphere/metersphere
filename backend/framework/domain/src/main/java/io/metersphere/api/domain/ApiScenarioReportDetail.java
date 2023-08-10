package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReportDetail implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "报告fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    private String reportId;

    @Schema(description =  "场景中各个步骤请求唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description =  "开始时间")
    private Long startTime;

    @Schema(description =  "结果状态")
    private String status;

    @Schema(description =  "单个请求步骤时间")
    private Long requestTime;

    @Schema(description =  "总断言数")
    private Long assertionsTotal;

    @Schema(description =  "失败断言数")
    private Long passAssertionsTotal;

    @Schema(description =  "误报编号")
    private String fakeCode;

    @Schema(description =  "请求名称")
    private String requestName;

    @Schema(description =  "环境fk")
    private String environmentId;

    @Schema(description =  "项目fk")
    private String projectId;

    @Schema(description =  "失败总数")
    private Integer errorTotal;

    @Schema(description =  "请求响应码")
    private String code;

    @Schema(description =  "执行结果")
    private byte[] content;

    private static final long serialVersionUID = 1L;
}