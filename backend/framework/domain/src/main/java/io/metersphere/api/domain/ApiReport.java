package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "API/CASE执行结果")
@Table("api_report")
@Data
public class ApiReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_report.id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口结果报告pk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{api_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.name.not_blank}", groups = {Created.class})
    @Schema(title = "接口报告名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 200]")
    private String name;

    @Size(min = 1, max = 50, message = "{api_report.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.resource_id.not_blank}", groups = {Created.class})
    @Schema(title = "资源fk/api_definition_id/api_test_case_id", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String resourceId;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_report.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String updateUser;

    @Size(min = 1, max = 1, message = "{api_report.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.deleted.not_blank}", groups = {Created.class})
    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 50, message = "{api_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.status.not_blank}", groups = {Created.class})
    @Schema(title = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String status;


    @Schema(title = "接口开始执行时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long startTime;


    @Schema(title = "接口执行结束时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long endTime;

    @Size(min = 1, max = 20, message = "{api_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.run_mode.not_blank}", groups = {Created.class})
    @Schema(title = "执行模块/API/CASE/API_PLAN", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 20]")
    private String runMode;

    @Size(min = 1, max = 50, message = "{api_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.pool_id.not_blank}", groups = {Created.class})
    @Schema(title = "资源池", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String poolId;

    @Size(min = 1, max = 50, message = "{api_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.trigger_mode.not_blank}", groups = {Created.class})
    @Schema(title = "触发模式/手动/批量/定时任务/JENKINS", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String triggerMode;


    @Schema(title = "版本fk", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 50, message = "{api_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_report.integrated_report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.integrated_report_id.not_blank}", groups = {Created.class})
    @Schema(title = "集成报告id/api_scenario_report_id", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String integratedReportId;

    @Size(min = 1, max = 1, message = "{api_report.integrated.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_report.integrated.not_blank}", groups = {Created.class})
    @Schema(title = "是否为集成报告，默认否", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 1]")
    private Boolean integrated;

}