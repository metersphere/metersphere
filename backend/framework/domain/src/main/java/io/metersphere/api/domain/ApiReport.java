package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiReport implements Serializable {
    @Schema(title = "接口结果报告pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "接口报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 200, message = "{api_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "资源fk/api_definition_id/api_test_case_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.resource_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "修改时间")
    private Long updateTime;

    @Schema(title = "创建人fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.update_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.update_user.length_range}", groups = {Created.class, Updated.class})
    private String updateUser;

    @Schema(title = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.deleted.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{api_report.deleted.length_range}", groups = {Created.class, Updated.class})
    private Boolean deleted;

    @Schema(title = "结果状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "接口开始执行时间")
    private Long startTime;

    @Schema(title = "接口执行结束时间")
    private Long endTime;

    @Schema(title = "执行模块/API/CASE/API_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.run_mode.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 20, message = "{api_report.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(title = "资源池fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.pool_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.pool_id.length_range}", groups = {Created.class, Updated.class})
    private String poolId;

    @Schema(title = "触发模式/手动/批量/定时任务/JENKINS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.trigger_mode.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(title = "版本fk")
    private String versionId;

    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "集成报告id/api_scenario_report_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.integrated_report_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_report.integrated_report_id.length_range}", groups = {Created.class, Updated.class})
    private String integratedReportId;

    @Schema(title = "是否为集成报告，默认否", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_report.integrated.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 1, message = "{api_report.integrated.length_range}", groups = {Created.class, Updated.class})
    private Boolean integrated;

    private static final long serialVersionUID = 1L;
}