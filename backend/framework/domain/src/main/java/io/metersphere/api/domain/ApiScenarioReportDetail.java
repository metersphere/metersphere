package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "场景报告步骤结果")
@Table("api_scenario_report_detail")
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioReportDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_report_detail.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report_detail.report_id.not_blank}", groups = {Created.class})
    @Schema(title = "报告fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportId;

    @Size(min = 1, max = 50, message = "{api_scenario_report_detail.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_report_detail.resource_id.not_blank}", groups = {Created.class})
    @Schema(title = "场景中各个步骤请求唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceId;

    @Schema(title = "开始时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long startTime;

    @Schema(title = "结果状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @Schema(title = "单个请求步骤时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long requestTime;

    @Schema(title = "总断言数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long assertionsTotal;

    @Schema(title = "失败断言数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long passAssertionsTotal;

    @Schema(title = "误报编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fakeCode;

    @Schema(title = "请求名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String requestName;

    @Schema(title = "环境fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentId;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String projectId;

    @Schema(title = "失败总数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer errorTotal;

    @Schema(title = "请求响应码", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String code;

    @Schema(title = "执行结果", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] content;

}