package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReport implements Serializable {
    @Schema(title = "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(title = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{load_test_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "报告描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "状态: Starting, Running, Error,Completed etc.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.status.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{load_test_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建人(执行人)ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.create_user.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.trigger_mode.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 64, message = "{load_test_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(title = "最大并发数")
    private String maxUsers;

    @Schema(title = "平均响应时间")
    private String avgResponseTime;

    @Schema(title = "每秒事务数")
    private String tps;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.project_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "测试名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_name.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 255, message = "{load_test_report.test_name.length_range}", groups = {Created.class, Updated.class})
    private String testName;

    @Schema(title = "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_resource_pool_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(title = "测试开始时间")
    private Long testStartTime;

    @Schema(title = "测试结束时间")
    private Long testEndTime;

    @Schema(title = "执行时长")
    private Long testDuration;

    @Schema(title = "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.version_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(title = "关联的测试计划报告ID（可以为空)")
    private String relevanceTestPlanReportId;

    private static final long serialVersionUID = 1L;
}